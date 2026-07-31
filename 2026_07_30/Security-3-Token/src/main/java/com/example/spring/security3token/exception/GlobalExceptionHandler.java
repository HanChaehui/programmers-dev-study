package com.example.spring.security3token.exception;

import com.example.spring.security3token.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<ErrorResponseDto> duplicateUserIdException(DuplicateUserIdException e) {
        log.warn("409응답 : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponseDto(HttpStatus.CONTINUE.value(), e.getMessage())
                );
    }
}
