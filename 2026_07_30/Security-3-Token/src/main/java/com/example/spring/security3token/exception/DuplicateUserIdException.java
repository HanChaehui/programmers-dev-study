package com.example.spring.security3token.exception;

public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(String message) {
        super(message);
    }
}
