package com.example.spring.security3token.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "hello";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

}