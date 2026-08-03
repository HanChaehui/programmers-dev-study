package com.example.spring.boardtoken.config;

import com.example.spring.boardtoken.config.filter.TokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/admin",
                                "/write",
                                "/detail",
                                "/update/**",
                                "/stats",

                                "/users/join",
                                "/users/login",

                                "/api/users/join",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/tokens/refresh",

                                "/api/boards/file/download/**",

                                "/css/**",
                                "/js/**",
                                "/access-denied",
                                "/error",

                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix() // "ROLE_" 접두사 자동 부착
                .role("ADMIN").implies("USER")
                .build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return ((request, response, accessDeniedException) -> {
            if ( request.getRequestURI().startsWith("/api") ) {
                sendError( response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다." );
            } else {
                response.sendRedirect("/access-denied");
            }
        });
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return ((request, response, authException) -> {
            if ( request.getRequestURI().startsWith("/api") ) {
                sendError( response, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다." );
            } else {
                response.sendRedirect("/access-denied");
            }
        });
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("status code : " + status + ", message : " + message);
    }

}
