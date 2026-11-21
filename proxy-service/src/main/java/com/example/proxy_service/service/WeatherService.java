package com.example.proxy_service.service;



import com.example.proxy_service.dto.ForecastItemResponse;
import com.example.proxy_service.dto.ForecastResponse;
import com.example.proxy_service.dto.GeocodingResponse;
import com.example.proxy_service.dto.weatherResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return new RestTemplate(factory);
    }

    public weatherResponse getCurrentWeather(double lat, double lon) {

        String url = "https://api.openweathermap.org/data/2.5/weather";

        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .encode()
                .toUri();

        JsonNode response;

        try {
            response = restTemplate.getForObject(
                    uri, JsonNode.class
            );
        } catch (RestClientResponseException exception) {
            throw handleWeatherApiError(exception);
        }

        catch (ResourceAccessException exception) {
            throw handleTimeout(exception);
        }
        // Check whether the API returned an empty response
        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Weather service returned an empty response"
            );
        }

        // Step 3: Convert API response into our DTO
        return new weatherResponse(
                response.path("name").asText(),
                response.path("main").path("temp").asDouble(),
                response.path("main").path("feels_like").asDouble(),
                response.path("main").path("humidity").asInt(),
                response.path("weather").path(0)
                        .path("description").asText(),
                response.path("wind").path("speed").asDouble()
        );
    }
    public weatherResponse getCurrentWeather(String city) {

        GeocodingResponse location = getCoordinates(city);

        return getCurrentWeather(
                location.getLatitude(),
                location.getLongitude()
        );
    }

    public GeocodingResponse getCoordinates(String city) {

        String url = "https://api.openweathermap.org/geo/1.0/direct";

        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("q", city)
                .queryParam("limit", 1)
                .queryParam("appid", apiKey)
                .build()
                .encode()
                .toUri();

        JsonNode response;

        try {
            response = restTemplate.getForObject(uri, JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw handleWeatherApiError(exception);
        }
        catch (ResourceAccessException exception) {
            throw handleTimeout(exception);
        }

        if (response == null || response.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "City not found"
            );
        }

        JsonNode location = response.path(0);

        return new GeocodingResponse(
                location.path("name").asText(),
                location.path("country").asText(),
                location.path("lat").asDouble(),
                location.path("lon").asDouble()
        );
    }
    public ForecastResponse getForecast(String city) {

        // First, get the city's latitude and longitude
        GeocodingResponse location = getCoordinates(city);

        String url = "https://api.openweathermap.org/data/2.5/forecast";

        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("lat", location.getLatitude())
                .queryParam("lon", location.getLongitude())
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build()
                .encode()
                .toUri();

        JsonNode response;

        try {
            response = restTemplate.getForObject(
                    uri, JsonNode.class
            );
        } catch (RestClientResponseException exception) {
            throw handleWeatherApiError(exception);
        }
        catch (ResourceAccessException exception) {
            throw handleTimeout(exception);
        }
        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Weather service returned an empty response"
            );
        }

        List<ForecastItemResponse> forecasts = new ArrayList<>();

        JsonNode forecastList = response.path("list");
        if (!forecastList.isArray() || forecastList.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Weather service returned no forecast data"
            );
        }

        for (JsonNode item : forecastList) {

            forecasts.add(new ForecastItemResponse(
                    item.path("dt_txt").asText(),
                    item.path("main").path("temp").asDouble(),
                    item.path("weather").path(0)
                            .path("description").asText()
            ));
        }

        return new ForecastResponse(
                response.path("city").path("name").asText(),
                forecasts
        );
    }
    // Convert OpenWeatherMap errors into appropriate Proxy Service responses.

    private ResponseStatusException handleWeatherApiError(
            RestClientResponseException exception) {

        if (exception.getStatusCode().value() == 401) {
            return new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Weather API authentication failed"
            );
        }

        if (exception.getStatusCode().value() == 429) {
            return new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Weather API rate limit exceeded"
            );
        }

        if (exception.getStatusCode().value() == 404) {
            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Weather data not found"
            );
        }

        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Weather service error"
        );
    }


    private ResponseStatusException handleTimeout(
            ResourceAccessException exception) {

        return new ResponseStatusException(
                HttpStatus.GATEWAY_TIMEOUT,
                "Weather service timed out. Please try again later."
        );
    }

}
