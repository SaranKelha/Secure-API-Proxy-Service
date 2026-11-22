
package com.example.proxy_service.dto;

import com.example.proxy_service.entity.Usage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminUsageResponse {

    private long totalRequests;

    private List<Object[]> requestsByUser;

    private List<Object[]> requestsByEndpoint;

    private List<Usage> recentRequests;
}
