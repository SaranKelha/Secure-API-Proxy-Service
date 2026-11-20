package com.example.proxy_service.service;

import com.example.proxy_service.entity.Usage;
import com.example.proxy_service.repository.UsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageService {
    private final UsageRepository usageRepository;
    public void saveUsage(String userEmail, String endpoint) {

        Usage usage = new Usage(
                userEmail,
                endpoint,
                LocalDateTime.now()
        );

        usageRepository.save(usage);
    }

    public List<Usage> getUserUsage(String userEmail) {
        return usageRepository.findByUserEmail(userEmail);
    }
    public long getTotalUsage() {
        return usageRepository.count();
    }
}
