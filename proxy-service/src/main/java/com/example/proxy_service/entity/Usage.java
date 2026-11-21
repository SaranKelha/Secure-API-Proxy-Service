package com.example.proxy_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import lombok.Getter;


import java.time.LocalDateTime;


@Entity
@Table(name = "api_usage")
@Getter

public class Usage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String endpoint;

    private LocalDateTime requestTime;
    public Usage() {
    }
    public Usage(String userEmail, String endpoint,
                 LocalDateTime requestTime) {
        this.userEmail = userEmail;
        this.endpoint = endpoint;
        this.requestTime = requestTime;
    }




}
