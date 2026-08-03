package com.example.spring.boardtoken.service;

import com.example.spring.boardtoken.config.security.CustomUserDetails;
import com.example.spring.boardtoken.domain.entity.User;
import com.example.spring.boardtoken.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("[로그인]" + username + "not found"));

        return CustomUserDetails.builder()
                .user(user)
                .build();
    }
}
