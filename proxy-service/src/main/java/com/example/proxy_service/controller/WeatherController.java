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
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM', 'ADMIN')")
    @GetMapping("/current")
    public weatherResponse getCurrentWeather(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            Authentication authentication) {

        if (city != null && !city.isBlank()) {

            if (lat != null || lon != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Provide either city or coordinates, not both"
                );
            }

            validateCity(city);

        } else if (lat != null && lon != null) {

            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid latitude or longitude"
                );
            }

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide a city name or both latitude and longitude"
            );
        }

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        rateLimitService.checkLimit(authentication.getName(), role);

        usageService.saveUsage(
                authentication.getName(),
                "/api/weather/current"
        );

        if (city != null && !city.isBlank()) {
            return weatherService.getCurrentWeather(city);
        }

        return weatherService.getCurrentWeather(lat, lon);
    }
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM','ADMIN')")
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
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
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
