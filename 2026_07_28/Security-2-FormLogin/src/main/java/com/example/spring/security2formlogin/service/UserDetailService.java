package com.example.spring.security2formlogin.service;

import com.example.spring.security2formlogin.config.security.CustomUserDetails;
import com.example.spring.security2formlogin.domain.entity.User;
import com.example.spring.security2formlogin.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findUserByUserId(username)
                .orElseThrow( () ->
                        new UsernameNotFoundException("[로그인]" + username + "not found")
                );

        return CustomUserDetails.builder()
                .user(user)
                .build();
    }
}
