package com.example.spring.springbootapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/weather")
    public List<Item> weather() {
        return weatherService.getCurrentWeather(60, 127);
    }
}
