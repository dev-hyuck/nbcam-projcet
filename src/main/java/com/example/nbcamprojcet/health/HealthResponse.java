package com.example.nbcamprojcet.health;

import lombok.Getter;

@Getter
public class HealthResponse {
    private final String status;
    private final String timestamp;

    public HealthResponse(String status, String timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }
}