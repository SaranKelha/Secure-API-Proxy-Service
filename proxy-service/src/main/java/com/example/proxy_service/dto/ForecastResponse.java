package com.example.proxy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class ForecastResponse {
    private String city;
    private List<ForecastItemResponse> forecasts;

}
