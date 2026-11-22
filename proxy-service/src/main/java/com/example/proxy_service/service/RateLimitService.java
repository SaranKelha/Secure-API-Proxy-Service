package com.example.proxy_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class RateLimitService {
    private final Map<String, RequestCounter> requestCounts =
            new HashMap<>();
    private long lastCleanupTime = 0;

    private static class RequestCounter {
        private int count;
        private long windowStart;

        public RequestCounter(long windowStart) {
            this.count = 0;
            this.windowStart = windowStart;
        }
    }

    public synchronized void checkLimit(String email, String role) {

        int limit;

        switch (role) {
            case "ROLE_PREMIUM":
                limit = 30;
                break;
            case "ROLE_ADMIN":
                limit = 60;
                break;
            default:
                limit = 10;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime >= 60000) {
            requestCounts.entrySet().removeIf(
                    entry -> currentTime - entry.getValue().windowStart >= 60000
            );
            lastCleanupTime = currentTime;
        }

        RequestCounter counter = requestCounts.get(email);

        if (counter == null ||
                currentTime - counter.windowStart >= 60000) {

            counter = new RequestCounter(currentTime);
            requestCounts.put(email, counter);
        }

        if (counter.count >= limit) {
            System.out.println("RATE LIMIT EXCEEDED for: " + email);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Try again later."
            );
        }

        counter.count++;
    }

}
