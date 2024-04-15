package com.example.gptStoryteller.api;

import com.example.gptStoryteller.dtos.MyResponse;
import com.example.gptStoryteller.dtos.SessionSettings;
import com.example.gptStoryteller.service.OpenAiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/_storyNoLimit")
@CrossOrigin(origins = "*")
public class StoryController {

  private final OpenAiService service;

  final static String SYSTEM_MESSAGE =
                    "You are a narrator for an interactive story."
                  + "The user will provide the character and the actions of the character."
                  + "You will continue the story based on the user's input."
                  + "The story should be engaging and interactive."
          ;

  public StoryController(OpenAiService service) {
    this.service = service;
  }

  @PostMapping
  public MyResponse getStory(@RequestParam SessionSettings session) {
    String system = String.format("%s. The story is set in %s. My character is %s", SYSTEM_MESSAGE,session.getSetting(), session.getCharacter());
    return service.makeRequest(session.getAction(),system);
  }
}
