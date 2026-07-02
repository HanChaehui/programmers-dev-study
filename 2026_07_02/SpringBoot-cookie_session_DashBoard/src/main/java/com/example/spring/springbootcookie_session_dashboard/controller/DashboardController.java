package com.example.spring.springbootcookie_session_dashboard.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            @CookieValue(value = "lastVisit", required = false) String lastVisit,
            @CookieValue(value = "theme", defaultValue = "light") String theme,
            HttpServletResponse response,
            Model model
    ) {
        // 테마
        model.addAttribute("theme", theme);

        // 사용자 이름
        if(session.getAttribute("username") == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", session.getAttribute("username"));

        // 지난 방문 기록
        if(lastVisit != null) {
            long ms = Long.parseLong(lastVisit);
            String MS = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(FMT);
            model.addAttribute("lastVisit", MS);
        }

        // 새 방문 기록 쿠키 저장
        Cookie cookie = new Cookie("lastVisit", String.valueOf(System.currentTimeMillis()));
        cookie.setMaxAge(30 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "dashboard";
    }

    @GetMapping("/theme")
    public String theme(
            @RequestParam String mode,
            HttpServletResponse response
    ) {
        String themeMode = "dark".equals(mode) ? "dark" : "light";
        Cookie cookie = new Cookie("theme", themeMode);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        return "redirect:/dashboard";
    }
}
