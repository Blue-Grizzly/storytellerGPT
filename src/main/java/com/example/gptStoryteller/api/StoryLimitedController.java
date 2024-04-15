package com.example.gptStoryteller.api;

import com.example.gptStoryteller.dtos.MyResponse;
import com.example.gptStoryteller.dtos.SessionSettings;
import com.example.gptStoryteller.service.OpenAiService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles fetching a joke via the ChatGPT API, but is IP-rate limited.
 */
@RestController
@RequestMapping("/api/v1/story")
@CrossOrigin(origins = "*")
public class StoryLimitedController {

  @Value("${app.bucket_capacity}")
  private int BUCKET_CAPACITY;

  @Value("${app.refill_amount}")
  private int REFILL_AMOUNT;

  @Value("${app.refill_time}")
  private int REFILL_TIME;

  private OpenAiService service;

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  /**
   * The controller called from the browser client.
   * @param service
   */
  public StoryLimitedController(OpenAiService service) {
    this.service=service;
  }

  /**
   * Creates the bucket for handling IP-rate limitations.
   * @return bucket
   */
  private Bucket createNewBucket() {
    Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, Refill.greedy(REFILL_AMOUNT, Duration.ofMinutes(REFILL_TIME)));
    return Bucket.builder().addLimit(limit).build();
  }

  private Bucket getBucket(String key) {
    return buckets.computeIfAbsent(key, k -> createNewBucket());
  }


  @PostMapping()
  public MyResponse getStoryLimited(@RequestBody SessionSettings session, HttpServletRequest request) {

    // Get the IP of the client.
    String ip = request.getRemoteAddr();
    // Get or create the bucket for the given IP/key.
    Bucket bucket = getBucket(ip);
    // Does the request adhere to the IP-rate  limitations?
    if (!bucket.tryConsume(1)) {
      // If not, tell the client "Too many requests".
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, try again later");
    }
    // Otherwise request to progress the story.

      String system = String.format("%s. The story is set in %s. My character is %s", StoryController.SYSTEM_MESSAGE,session.getSetting(), session.getCharacter());
      return service.makeRequest(session.getAction(),system);
  }
}
