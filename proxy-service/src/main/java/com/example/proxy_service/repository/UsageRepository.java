package com.example.proxy_service.repository;

import com.example.proxy_service.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsageRepository extends JpaRepository<Usage,Long> {
    List<Usage> findByUserEmail(String userEmail);
    long count();
}
