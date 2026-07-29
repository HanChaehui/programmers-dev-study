package com.example.spring.security2formlogin.dto;

import com.example.spring.security2formlogin.domain.entity.Role;
import com.example.spring.security2formlogin.domain.entity.User;
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
