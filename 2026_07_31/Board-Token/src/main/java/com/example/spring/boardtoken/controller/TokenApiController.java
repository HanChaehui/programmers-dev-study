package com.example.spring.boardtoken.controller;

import com.example.spring.boardtoken.config.jwt.JwtProperties;
import com.example.spring.boardtoken.dto.ErrorResponseDto;
import com.example.spring.boardtoken.dto.RefreshTokenResponseDto;
import com.example.spring.boardtoken.service.TokenService;
import com.example.spring.boardtoken.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
public class TokenApiController {

    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        RefreshTokenResponseDto refreshTokenResponseDto = tokenService.refreshToken(request.getCookies());

        if ( refreshTokenResponseDto.isValidated() ) {

            CookieUtil.addCookie(
                    response,
                    CookieUtil.REFRESH_TOKEN_COOKIE,
                    refreshTokenResponseDto.getRefreshToken(),
                    (int) jwtProperties.getRefreshTokenValidity().toSeconds()
            );
            refreshTokenResponseDto.setRefreshToken(null);
            return ResponseEntity.ok(refreshTokenResponseDto);
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다."));
    }
}
