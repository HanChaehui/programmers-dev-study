package com.example.spring.security3token.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String message;
}
