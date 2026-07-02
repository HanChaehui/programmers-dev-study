package com.example.spring.springbootcookie_session_dashboard.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.desktop.ScreenSleepEvent;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm(
            HttpSession session
    ) {
        if(session.getAttribute("username") != null){
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            HttpSession session
    ) {
        session.setAttribute("username", username);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(
            HttpSession session
    ) {
        session.invalidate();
        return "redirect:/login";
    }
}
