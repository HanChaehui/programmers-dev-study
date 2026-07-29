package com.example.spring.security2formlogin.controller;

import com.example.spring.security2formlogin.dto.SignUpRequestDto;
import com.example.spring.security2formlogin.dto.SignUpResponseDto;
import com.example.spring.security2formlogin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @PostMapping("/join")
    public SignUpResponseDto join(@RequestBody SignUpRequestDto signUpRequestDto) {
        userService.signUp(signUpRequestDto);
        return SignUpResponseDto.builder()
                .url("/users/login")
                .build();
    }
}
