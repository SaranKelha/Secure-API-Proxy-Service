package com.example.proxy_service.controller;

import com.example.proxy_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UsageService usageService;

    @GetMapping("/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalUsage() {
        return usageService.getTotalUsage();
    }
}
