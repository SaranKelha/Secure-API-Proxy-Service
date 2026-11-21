
package com.example.proxy_service;

import com.example.proxy_service.entity.Usage;
import com.example.proxy_service.repository.UsageRepository;
import com.example.proxy_service.service.UsageService;
import com.example.proxy_service.service.RateLimitService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProxyServiceApplicationTests {

	@Mock
	private UsageRepository usageRepository;

	@InjectMocks
	private UsageService usageService;

	private final RateLimitService rateLimitService =
			new RateLimitService();

	// RATE LIMIT TESTS

	@Test
	void userRateLimitExceededAfter10Requests() {

		for (int i = 0; i < 10; i++) {
			rateLimitService.checkLimit("user@gmail.com", "ROLE_USER");
		}

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> rateLimitService.checkLimit(
						"user@gmail.com", "ROLE_USER")
		);

		assertEquals(HttpStatus.TOO_MANY_REQUESTS,
				exception.getStatusCode());
	}

	@Test
	void premiumRateLimitExceededAfter30Requests() {

		for (int i = 0; i < 30; i++) {
			rateLimitService.checkLimit(
					"premium@gmail.com", "ROLE_PREMIUM");
		}

		assertThrows(ResponseStatusException.class,
				() -> rateLimitService.checkLimit(
						"premium@gmail.com", "ROLE_PREMIUM"));
	}

	@Test
	void adminRateLimitExceededAfter60Requests() {

		for (int i = 0; i < 60; i++) {
			rateLimitService.checkLimit(
					"admin@gmail.com", "ROLE_ADMIN");
		}

		assertThrows(ResponseStatusException.class,
				() -> rateLimitService.checkLimit(
						"admin@gmail.com", "ROLE_ADMIN"));
	}

	// USAGE SERVICE TESTS

	@Test
	void saveUsageSuccessfully() {

		usageService.saveUsage(
				"sara@gmail.com", "/api/weather/current");

		verify(usageRepository).save(argThat(usage ->
				usage.getUserEmail().equals("sara@gmail.com") &&
						usage.getEndpoint().equals("/api/weather/current") &&
						usage.getRequestTime() != null
		));
	}

	@Test
	void getUserUsageSuccessfully() {

		Usage usage = new Usage(
				"sara@gmail.com",
				"/api/weather/current",
				LocalDateTime.now()
		);

		when(usageRepository.findByUserEmail("sara@gmail.com"))
				.thenReturn(List.of(usage));

		List<Usage> result =
				usageService.getUserUsage("sara@gmail.com");

		assertEquals(1, result.size());
		assertEquals("sara@gmail.com",
				result.get(0).getUserEmail());

		verify(usageRepository).findByUserEmail("sara@gmail.com");
	}

	@Test
	void getTotalUsageSuccessfully() {

		when(usageRepository.count()).thenReturn(5L);

		long result = usageService.getTotalUsage();

		assertEquals(5L, result);
		verify(usageRepository).count();
	}
}
