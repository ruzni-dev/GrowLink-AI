package com.growlink.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/api/health")
    public Map<String, String> health() {

        return Map.of(
                "application", "GrowLink AI",
                "status", "UP",
                "message", "Backend is running successfully"
        );
    }
}
