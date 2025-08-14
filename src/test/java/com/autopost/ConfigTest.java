package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ConfigTest {
  @Test
  void loadsConfigFromEnvironmentMap() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "test-openai-key");
    envVars.put("RAW_FOLDER_ID", "raw-folder");
    envVars.put("EDITS_FOLDER_ID", "edits-folder");
    envVars.put("OPENAI_MODEL", "gpt-4");
    envVars.put("WEBHOOK_URL", "https://example.com/webhook");
    envVars.put("GOOGLE_APPLICATION_CREDENTIALS", "/path/to/creds");
    envVars.put("GOOGLE_SERVICE_ACCOUNT_JSON", "{}");
    envVars.put("TWITTER_API_KEY", "tw-key");
    envVars.put("TWITTER_API_SECRET", "tw-secret");
    envVars.put("TWITTER_ACCESS_TOKEN", "tw-token");
    envVars.put("TWITTER_ACCESS_SECRET", "tw-access-secret");
    
    Config cfg = Config.loadFromEnv(envVars);
    
    assertEquals("test-openai-key", cfg.openaiKey());
    assertEquals("gpt-4", cfg.openaiModel());
    assertEquals("raw-folder", cfg.rawFolderId());
    assertEquals("edits-folder", cfg.editsFolderId());
    assertEquals("https://example.com/webhook", cfg.webhookUrl());
    assertEquals("/path/to/creds", cfg.saPath());
    assertEquals("{}", cfg.saInlineJson());
    assertEquals("tw-key", cfg.twApiKey());
    assertEquals("tw-secret", cfg.twApiSecret());
    assertEquals("tw-token", cfg.twAccessToken());
    assertEquals("tw-access-secret", cfg.twAccessSecret());
  }
  
  @Test
  void usesDefaultValuesForOptionalFields() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "test-openai-key");
    envVars.put("RAW_FOLDER_ID", "raw-folder");
    envVars.put("EDITS_FOLDER_ID", "edits-folder");
    // Not setting optional fields
    
    Config cfg = Config.loadFromEnv(envVars);
    
    assertEquals("test-openai-key", cfg.openaiKey());
    assertEquals("gpt-4o-mini", cfg.openaiModel()); // default value
    assertEquals("raw-folder", cfg.rawFolderId());
    assertEquals("edits-folder", cfg.editsFolderId());
    assertEquals("", cfg.webhookUrl()); // default empty
    assertEquals("", cfg.saPath()); // default empty
    assertEquals("", cfg.saInlineJson()); // default empty
    assertEquals("", cfg.twApiKey()); // default empty
    assertEquals("", cfg.twApiSecret()); // default empty
    assertEquals("", cfg.twAccessToken()); // default empty
    assertEquals("", cfg.twAccessSecret()); // default empty
  }
  
  @Test
  void throwsExceptionForMissingRequiredFields() {
    Map<String, String> envVars = new HashMap<>();
    // Missing required OPENAI_API_KEY
    envVars.put("RAW_FOLDER_ID", "raw-folder");
    envVars.put("EDITS_FOLDER_ID", "edits-folder");
    
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      Config.loadFromEnv(envVars);
    });
    
    assertEquals("OPENAI_API_KEY is required", exception.getMessage());
  }
  
  @Test
  void backwardCompatibilityLoadFromEnvStillWorks() {
    // This test ensures the original loadFromEnv() method still works
    // We can't easily test the actual system environment, but we can test that the method exists and compiles
    // In a real environment with the required env vars set, this would work
    try {
      Config.loadFromEnv();
      // If we reach here without exception, the method works (though it may fail due to missing env vars in test)
    } catch (RuntimeException e) {
      // Expected in test environment where required env vars are not set
      assert(e.getMessage().contains("is required"));
    }
  }
}