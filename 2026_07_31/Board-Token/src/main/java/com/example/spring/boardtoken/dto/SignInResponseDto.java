package com.example.spring.boardtoken.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInResponseDto {
    private boolean isLoggedIn;
    private String url;
    private String message;
    private String userName;
    private String userId;
    private String accessToken;
    private String refreshToken;
}
