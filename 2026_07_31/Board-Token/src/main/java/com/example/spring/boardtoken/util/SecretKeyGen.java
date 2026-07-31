package com.example.spring.boardtoken.util;

import java.security.SecureRandom;
import java.util.Base64;

// 테스트코드 쪽으로 빼기

public class SecretKeyGen {

    private static final int KEY_LENGTH_BYTES = 64;

    static void main(String[] args) {
        byte[] bytes = new byte[KEY_LENGTH_BYTES];

        new SecureRandom().nextBytes(bytes);

        System.out.println( Base64.getEncoder().encodeToString(bytes) );
    }

}