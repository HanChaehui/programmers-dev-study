package com.example.spring.boardtoken.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    public int status;
    private String message;
}
