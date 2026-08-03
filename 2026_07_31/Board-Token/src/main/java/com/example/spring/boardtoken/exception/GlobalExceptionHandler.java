package com.example.spring.boardtoken.exception;

import com.example.spring.boardtoken.dto.ErrorResponseDto;
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
        log.warn("409 응답 : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponseDto(HttpStatus.CONFLICT.value(), e.getMessage())
                );
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> boardNotFoundException(BoardNotFoundException e) {
        log.warn("404 응답 : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), e.getMessage())
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> exception(Exception e) {
        log.error("500 응답(예상치 못한 예외 발생)", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다.")
                );
    }

}