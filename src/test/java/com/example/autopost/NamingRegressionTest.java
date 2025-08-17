package com.autopost;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ConfigIntegrationTest {
  
  @Test
  void driveServiceThrowsRuntimeExceptionWithInvalidJson() {
    // Create config with invalid JSON for service account
    Config cfg = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", 
        "invalid json content", // invalid JSON
        "", "", "", "", "public", "secret"
    );
    
    // DriveService constructor should fail when trying to use invalid JSON
    Exception exception = assertThrows(Exception.class, () -> new DriveService(cfg));
    
    // The exception should be a RuntimeException with our validation message
    assertTrue(exception instanceof RuntimeException);
    assertTrue(exception.getMessage().contains("GOOGLE_SERVICE_ACCOUNT_JSON contains invalid JSON format"));
  }
  
  @Test
  void driveServiceThrowsRuntimeExceptionWithNoCredentials() {
    // Create config with no credentials
    Config cfg = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", 
        "", // empty JSON
        "", "", "", "", "public", "secret"
    );
    
    // DriveService constructor should fail when no credentials are provided
    Exception exception = assertThrows(Exception.class, () -> new DriveService(cfg));
    
    // The exception should be about missing credentials
    assertTrue(exception instanceof RuntimeException);
    assertTrue(exception.getMessage().contains("Service account credentials not provided"));
  }
  
  @Test
  void configWithValidJsonPassesValidation() {
    // Create config with valid JSON
    String validJson = "{\"type\": \"service_account\", \"project_id\": \"test\"}";
    Config cfg = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", 
        validJson, // valid JSON
        "", "", "", "", "public", "secret"
    );
    
    // hasInlineSA should return true without throwing
    assertTrue(cfg.hasInlineSA());
    
    // Note: We can't fully test DriveService construction without actual Google credentials,
    // but we can verify that the validation passes and the config is considered valid
  }
}
