package com.autopost.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path nextRunPath = Paths.get("./state/next_run.json");
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", ZonedDateTime.now().toString());
        
        try {
            if (Files.exists(nextRunPath)) {
                String content = Files.readString(nextRunPath);
                ObjectNode nextRun = (ObjectNode) objectMapper.readTree(content);
                
                health.put("next_run_at", nextRun.get("timestamp").asText());
                health.put("strategy", nextRun.get("strategy").asText());
                health.put("confidence", nextRun.get("confidence").asDouble());
                health.put("reason", nextRun.get("reason").asText());
            } else {
                health.put("next_run_at", "not scheduled");
                health.put("strategy", "llm");
            }
        } catch (Exception e) {
            health.put("error", e.getMessage());
        }
        
        return health;
    }
    
    @GetMapping("/")
    public String index() {
        return "AutoPost Service - LLM Driven Scheduling";
    }
}