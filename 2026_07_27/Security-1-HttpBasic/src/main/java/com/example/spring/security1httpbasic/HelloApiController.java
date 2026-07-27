package com.example.spring.security1httpbasic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
