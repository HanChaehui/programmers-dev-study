package com.example.spring.board1signup.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    @GetMapping("/")
    public String boardList(HttpSession session, Model model) {
        setSession(session, model);
        return "board-list";
    }


    private void setSession(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);
    }
}
