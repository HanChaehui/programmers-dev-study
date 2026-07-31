package com.example.spring.security3token.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenResponseDto {

    private boolean validated;
    private String accessToken;
    private String refreshToken;
}
