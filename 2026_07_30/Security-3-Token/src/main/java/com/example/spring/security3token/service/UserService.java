package com.example.spring.security3token.service;

import com.example.spring.security3token.config.security.CustomUserDetails;
import com.example.spring.security3token.domain.entity.User;
import com.example.spring.security3token.domain.repository.UserRepository;
import com.example.spring.security3token.dto.SignInRequestDto;
import com.example.spring.security3token.dto.SignInResponseDto;
import com.example.spring.security3token.dto.SignUpRequestDto;
import com.example.spring.security3token.dto.SignUpResponseDto;
import com.example.spring.security3token.exception.DuplicateUserIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        if(userRepository.existsByUserId(signUpRequestDto.getUserId())) {
            throw new DuplicateUserIdException("[회원가입] 이미 사용 중인 ID입니다.");
        }
        User user = signUpRequestDto.toUser(passwordEncoder.encode(signUpRequestDto.getPassword()));
        userRepository.save(user);
    }

    public SignInResponseDto signIn(SignInRequestDto signInRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequestDto.getUserId(), signInRequestDto.getPassword())
        );

        User user = ((CustomUserDetails)authenticate.getPrincipal()).getUser();
        TokenService.TokenPair tokenPair = tokenService.issueToken(user);

        return SignInResponseDto.builder()
                .isLoggedIn(true)
                .message("로그인 성공")
                .url("/")
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .userName(user.getName())
                .userId(user.getUserId())
                .build();
    }
}
