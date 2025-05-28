package com.torah.torahAI.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {
    
    @GetMapping(path = "/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    
}
