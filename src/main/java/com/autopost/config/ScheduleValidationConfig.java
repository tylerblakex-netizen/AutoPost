package com.autopost.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.CronExpression;

import jakarta.annotation.PostConstruct;

@Configuration
public class ScheduleValidationConfig {
    
    @Value("${auto.post.cron:0 0 18 * * *}")
    private String autoPostCron;
    
    @PostConstruct
    public void validateCronExpression() {
        try {
            CronExpression.parse(autoPostCron);
            System.out.println("✅ Cron expression validation passed: " + autoPostCron);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format(
                "❌ Invalid cron expression in AUTO_POST_CRON: '%s'. Error: %s", 
                autoPostCron, e.getMessage()
            );
            System.err.println(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}