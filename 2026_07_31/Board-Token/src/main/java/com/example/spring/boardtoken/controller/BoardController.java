package com.example.spring.boardtoken.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {

    @GetMapping("/")
    public String boardList(HttpSession session, Model model) {
        setSession(session, model);
        return "board-list";
    }

    @GetMapping("/write")
    public String write(HttpSession session, Model model) {
        setSession(session, model);
        return "/board-write";
    }

    @GetMapping("/detail")
    public String detail(
            @RequestParam("id") Long id,
            HttpSession session,
            Model model
    ) {
        setSession(session, model);
        model.addAttribute("id", id);
        return "/board-detail";
    }

    @GetMapping("/update/{id}")
    public String update(
            @PathVariable long id,
            HttpSession session,
            Model model
    ) {
        setSession(session, model);
        model.addAttribute("id", id);
        return "/board-update";
    }

    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {
        setSession(session, model);
        return "/board-stats";
    }

    private void setSession(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);
    }
}