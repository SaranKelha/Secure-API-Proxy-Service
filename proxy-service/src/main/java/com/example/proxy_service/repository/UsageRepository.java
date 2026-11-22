
package com.example.proxy_service.repository;

import com.example.proxy_service.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsageRepository extends JpaRepository<Usage, Long> {

    List<Usage> findByUserEmail(String userEmail);

    long count();

    @Query("SELECT u.userEmail, COUNT(u) FROM Usage u GROUP BY u.userEmail")
    List<Object[]> countRequestsByUser();

    @Query("SELECT u.endpoint, COUNT(u) FROM Usage u GROUP BY u.endpoint")
    List<Object[]> countRequestsByEndpoint();

    List<Usage> findTop10ByOrderByRequestTimeDesc();
}
