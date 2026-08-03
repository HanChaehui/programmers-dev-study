package com.example.spring.boardtoken.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutResponseDto {
    String message;
    String url;
}