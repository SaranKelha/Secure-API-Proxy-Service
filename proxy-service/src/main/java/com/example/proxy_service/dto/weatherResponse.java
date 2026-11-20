package com.example.proxy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class weatherResponse {

    private String city;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;
    private double windSpeed;


}
