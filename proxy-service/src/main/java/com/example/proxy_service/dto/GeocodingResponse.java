package com.example.proxy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeocodingResponse {
    private String name;
    private String country;
    private double latitude;
    private double longitude;
}
