package com.example.spring.security3token.service;

import com.example.spring.security3token.config.jwt.JwtProperties;
import com.example.spring.security3token.config.jwt.TokenProvider;
import com.example.spring.security3token.config.jwt.TokenStatus;
import com.example.spring.security3token.domain.entity.User;
import com.example.spring.security3token.dto.RefreshTokenResponseDto;
import com.example.spring.security3token.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    public record TokenPair(String accessToken, String refreshToken) {}

    public TokenPair issueToken(User user) {
        String accessToken = tokenProvider.generateToken(user, jwtProperties.getAccessTokenValidity());
        String refreshToken = tokenProvider.generateToken(user, jwtProperties.getRefreshTokenValidity());
        return new TokenPair(accessToken, refreshToken);
    }

    private String getRefreshToken(Cookie[] cookies) {

        if(cookies == null) return null;

        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(CookieUtil.REFRESH_TOKEN_COOKIE)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public RefreshTokenResponseDto refreshToken(Cookie[] cookies) {
        String refreshToken = getRefreshToken(cookies);

        if(refreshToken != null && tokenProvider.validateToken(refreshToken) == TokenStatus.VALID) {

            User user = tokenProvider.getTokenDetails(refreshToken);

            TokenPair tokenPair = issueToken(user);

            return RefreshTokenResponseDto.builder()
                    .validated(true)
                    .accessToken(tokenPair.accessToken)
                    .refreshToken(tokenPair.refreshToken)
                    .build();
        }

        return RefreshTokenResponseDto.builder()
                .validated(false)
                .build();
    }

}
