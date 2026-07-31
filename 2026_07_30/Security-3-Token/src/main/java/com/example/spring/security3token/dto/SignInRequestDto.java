package com.example.spring.security3token.dto;

import lombok.Getter;

@Getter
public class SignInRequestDto {
    private String userId;
    private String password;
}
