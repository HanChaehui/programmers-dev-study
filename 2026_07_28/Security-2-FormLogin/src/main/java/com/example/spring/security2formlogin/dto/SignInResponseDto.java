package com.example.spring.security2formlogin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInResponseDto {
    private boolean isLoggedIn;
    private String url;
    private String message;
    private String userName;
    private String userId;
}
