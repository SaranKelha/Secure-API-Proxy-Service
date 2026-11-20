package com.example.proxy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForecastItemResponse {
    private String dateTime;
    private double temperature;
    private String description;
}
