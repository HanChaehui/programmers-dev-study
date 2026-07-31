package com.example.spring.security3token.dto;

import com.example.spring.security3token.domain.entity.User;
import lombok.Getter;

@Getter
public class SignUpRequestDto {
    private String userId;
    private String userName;
    private String password;

    public User toUser(String encodedPassword) {
        return User.builder()
                .userId(userId)
                .name(userName)
                .password(encodedPassword)
                .build();
    }
}
