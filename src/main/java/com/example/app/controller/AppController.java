package com.example.app.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AppController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from CIE4 CI/CD Pipeline!");
        return response;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        Map<String, String> response = new HashMap<>();
        response.put("version", "1.0.0");
        response.put("application", "cie4-java-app");
        return response;
    }

    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("echo", body);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "CIE4 Java Maven App");
        response.put("framework", "Spring Boot");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("endpoints", Arrays.asList("/api/health", "/api/hello", "/api/version", "/api/echo", "/api/info"));
        return response;
    }
}
