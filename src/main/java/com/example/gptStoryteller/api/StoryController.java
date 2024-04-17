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

@RestController
@RequestMapping("/api/v1/story")
@CrossOrigin(origins = "*")
public class StoryController {

  private final OpenAiService service;

  final static String SYSTEM_MESSAGE =
                    "You are a narrator for an interactive story."
                  + " You will continue the story based on the user's input."
                  + " The story should be engaging and interactive."
                  + " Your response should be as short."
                  + " When asked about the setting, reply with the setting of the story."
                  + " When asked about the character, reply with the character of the story."
                  + " The user will play as a character and respond with their actions."
                  + " Leave the options open ended for the user."
                  + " You will address the user as their character."
                  + " Do not provide options for what to do next"

          ;
  public StoryController(OpenAiService service) {
    this.service = service;
  }

  @Value("${app.bucket_capacity}")
  private int BUCKET_CAPACITY;

  @Value("${app.refill_amount}")
  private int REFILL_AMOUNT;

  @Value("${app.refill_time}")
  private int REFILL_TIME;

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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
    String system;
    if (session.getHistory() != null){
      system = String.format("%s. The story is set in %s. My character is %s. The story so far is %s", StoryController.SYSTEM_MESSAGE, session.getSetting(), session.getCharacter(), session.getHistory());
    } else {
      system = String.format("%s. The story is set in %s. My character is %s.", StoryController.SYSTEM_MESSAGE, session.getSetting(), session.getCharacter());
      session.setAction("Start the story");
    }
    return service.makeRequest(session.getAction(),system);
  }
}


