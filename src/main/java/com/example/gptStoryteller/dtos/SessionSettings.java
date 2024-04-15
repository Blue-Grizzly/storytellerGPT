package com.example.gptStoryteller.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionSettings {
    private String character;
    private String setting;
    private String action;

}
