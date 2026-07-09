package com.example.spring.board1signup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private boolean successed;
    private String url;
    private String message;

    public static LoginResponseDto success() {
        return new LoginResponseDto(true, "/", "로그인에 성공했습니다.");
    }

    public static LoginResponseDto fail() {
        return new LoginResponseDto(false, null, "아이디 또는 비밀번확 일치하지 않습니다.");
    }
}
