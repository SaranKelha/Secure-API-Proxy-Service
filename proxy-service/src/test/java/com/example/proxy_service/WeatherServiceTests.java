
package com.example.proxy_service;

import com.example.proxy_service.dto.GeocodingResponse;
import com.example.proxy_service.service.WeatherService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WeatherServiceTests {

    private WeatherService weatherService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {

        weatherService = new WeatherService();

        ReflectionTestUtils.setField(
                weatherService, "apiKey", "test-api-key");

        RestTemplate restTemplate =
                (RestTemplate) ReflectionTestUtils.getField(
                        weatherService, "restTemplate");

        mockServer = MockRestServiceServer
                .bindTo(restTemplate)
                .build();
    }

    @Test
    void getCoordinatesSuccessfully() {

        String jsonResponse = """
                [
                    {
                        "name": "London",
                        "country": "GB",
                        "lat": 51.5074,
                        "lon": -0.1278
                    }
                ]
                """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/geo/1.0/direct")))
                .andRespond(withSuccess(
                        jsonResponse,
                        MediaType.APPLICATION_JSON));

        GeocodingResponse result =
                weatherService.getCoordinates("London");

        assertEquals("London", result.getName());
        assertEquals("GB", result.getCountry());
        assertEquals(51.5074, result.getLatitude());
        assertEquals(-0.1278, result.getLongitude());

        mockServer.verify();
    }

    @Test
    void getCoordinatesCityNotFound() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/geo/1.0/direct")))
                .andRespond(withSuccess(
                        "[]",
                        MediaType.APPLICATION_JSON));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCoordinates("UnknownCity")
                );

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void getCurrentWeatherSuccessfully() {

        String jsonResponse = """
            {
                "name": "London",
                "main": {
                    "temp": 15.5,
                    "feels_like": 14.2,
                    "humidity": 70
                },
                "weather": [
                    {
                        "description": "light rain"
                    }
                ],
                "wind": {
                    "speed": 3.5
                }
            }
            """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withSuccess(
                        jsonResponse,
                        MediaType.APPLICATION_JSON));

        var result = weatherService.getCurrentWeather(51.5074, -0.1278);

        assertEquals("London", result.getCity());
        assertEquals(15.5, result.getTemperature());
        assertEquals(14.2, result.getFeelsLike());
        assertEquals(70, result.getHumidity());
        assertEquals("light rain", result.getDescription());
        assertEquals(3.5, result.getWindSpeed());

        mockServer.verify();
    }

    @Test
    void getCurrentWeatherEmptyResponse() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withSuccess());

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void getForecastSuccessfully() {

        String geocodingResponse = """
            [
                {
                    "name": "London",
                    "country": "GB",
                    "lat": 51.5074,
                    "lon": -0.1278
                }
            ]
            """;

        String forecastResponse = """
            {
                "city": {
                    "name": "London"
                },
                "list": [
                    {
                        "dt_txt": "2026-11-22 12:00:00",
                        "main": {
                            "temp": 16.5
                        },
                        "weather": [
                            {
                                "description": "clear sky"
                            }
                        ]
                    }
                ]
            }
            """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/geo/1.0/direct")))
                .andRespond(withSuccess(
                        geocodingResponse,
                        MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/forecast")))
                .andRespond(withSuccess(
                        forecastResponse,
                        MediaType.APPLICATION_JSON));

        var result = weatherService.getForecast("London");

        assertEquals("London", result.getCity());
        assertEquals(1, result.getForecasts().size());

        assertEquals(
                "2026-11-22 12:00:00",
                result.getForecasts().get(0).getDateTime()
        );

        assertEquals(
                16.5,
                result.getForecasts().get(0).getTemperature()
        );

        assertEquals(
                "clear sky",
                result.getForecasts().get(0).getDescription()
        );

        mockServer.verify();
    }

    @Test
    void getForecastEmptyData() {

        String geocodingResponse = """
            [
                {
                    "name": "London",
                    "country": "GB",
                    "lat": 51.5074,
                    "lon": -0.1278
                }
            ]
            """;

        String forecastResponse = """
            {
                "city": {
                    "name": "London"
                },
                "list": []
            }
            """;

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/geo/1.0/direct")))
                .andRespond(withSuccess(
                        geocodingResponse,
                        MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/forecast")))
                .andRespond(withSuccess(
                        forecastResponse,
                        MediaType.APPLICATION_JSON));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getForecast("London")
                );

        assertEquals(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void weatherApiAuthenticationError() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withStatus(
                        org.springframework.http.HttpStatus.UNAUTHORIZED));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void weatherApiRateLimitError() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withStatus(
                        org.springframework.http.HttpStatus.TOO_MANY_REQUESTS));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void weatherApiServerError() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withStatus(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void weatherApiTimeout() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(request ->
                {
                    throw new org.springframework.web.client.ResourceAccessException(
                            "Connection timed out");
                });

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT,
                exception.getStatusCode()
        );

        mockServer.verify();
    }

    @Test
    void weatherApiNotFoundError() {

        mockServer.expect(requestTo(
                        org.hamcrest.Matchers.containsString(
                                "api.openweathermap.org/data/2.5/weather")))
                .andRespond(withStatus(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        org.springframework.web.server.ResponseStatusException exception =
                assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> weatherService.getCurrentWeather(51.5074, -0.1278)
                );

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        mockServer.verify();
    }









}
