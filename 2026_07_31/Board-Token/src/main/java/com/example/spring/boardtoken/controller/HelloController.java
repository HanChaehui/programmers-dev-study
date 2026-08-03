package com.example.spring.boardtoken.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

}