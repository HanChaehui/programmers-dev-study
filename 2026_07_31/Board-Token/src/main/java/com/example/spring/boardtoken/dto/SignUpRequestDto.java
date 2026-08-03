package com.example.spring.boardtoken.dto;

import com.example.spring.boardtoken.domain.entity.Role;
import com.example.spring.boardtoken.domain.entity.User;
import lombok.Getter;

@Getter
public class SignUpRequestDto {

    private String userId;
    private String password;
    private String userName;
    private Role role;

    public User toUser(String encodedPassword) {
        return User.builder()
                .userId(userId)
                .password(encodedPassword)
                .name(userName)
                .role(role != null ? role : Role.ROLE_USER)
                .build();
    }
}
