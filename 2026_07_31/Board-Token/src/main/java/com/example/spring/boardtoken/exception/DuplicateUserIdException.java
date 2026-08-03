package com.example.spring.boardtoken.exception;

public class DuplicateUserIdException extends RuntimeException{
    public DuplicateUserIdException(String message) {
        super(message);
    }
}
