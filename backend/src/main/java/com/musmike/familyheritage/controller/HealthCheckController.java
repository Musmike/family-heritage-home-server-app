package com.musmike.familyheritage.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@CrossOrigin(origins = "http://localhost:5173")
public class HealthCheckController {

    @GetMapping
    public Map<String, String> checkHealth() {
        return Map.of("message", "Hello from Backend!");
    }
}