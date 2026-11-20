package com.example.proxy_service.controller;

import com.example.proxy_service.dto.ForecastResponse;
import com.example.proxy_service.dto.GeocodingResponse;
import com.example.proxy_service.dto.weatherResponse;
import com.example.proxy_service.service.RateLimitService;
import com.example.proxy_service.service.UsageService;
import com.example.proxy_service.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;
    private final UsageService usageService;
    private final RateLimitService rateLimitService;
    @GetMapping("/current")
    public weatherResponse getCurrentWeather(
            @RequestParam String city,
            Authentication authentication) {
        validateCity(city);

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        rateLimitService.checkLimit(authentication.getName(), role);

        usageService.saveUsage(
                authentication.getName(),
                "/api/weather/current"
        );

        return weatherService.getCurrentWeather(city);
    }
    @GetMapping("/search")
    public GeocodingResponse searchCity(
            @RequestParam String city,
            Authentication authentication) {
        validateCity(city);

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        rateLimitService.checkLimit(authentication.getName(), role);

        usageService.saveUsage(
                authentication.getName(),
                "/api/weather/search"
        );

        return weatherService.getCoordinates(city);
    }
    @GetMapping("/forecast")
    @PreAuthorize("hasRole('PREMIUM')")
    public ForecastResponse getForecast(
            @RequestParam String city,
            Authentication authentication) {
        validateCity(city);

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        rateLimitService.checkLimit(authentication.getName(), role);

        usageService.saveUsage(
                authentication.getName(),
                "/api/weather/forecast"
        );

        return weatherService.getForecast(city);
    }
    private void validateCity(String city) {

        if (city == null || city.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City name cannot be empty"
            );
        }

        if (city.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City name is too long"
            );
        }
    }

}
