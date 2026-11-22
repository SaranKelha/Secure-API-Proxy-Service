
package com.example.proxy_service.controller;

import com.example.proxy_service.dto.AdminUsageResponse;
import com.example.proxy_service.entity.Usage;
import com.example.proxy_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/me")
    public List<Usage> getMyUsage(Authentication authentication) {
        return usageService.getUserUsage(authentication.getName());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUsageResponse> getAdminUsage() {

        AdminUsageResponse response = new AdminUsageResponse(
                usageService.getTotalUsage(),
                usageService.getUsageByUser(),
                usageService.getUsageByEndpoint(),
                usageService.getRecentUsage()
        );

        return ResponseEntity.ok(response);
    }
}
