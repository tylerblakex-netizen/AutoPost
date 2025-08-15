package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.HashMap;

public class ConfigTest {
  @Test
  void loadsServiceCredentialsFromSystemProperties() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    Config cfg = Config.loadFromSystemProperties();
    assertEquals("public", cfg.servicePublicId());
    assertEquals("secret", cfg.serviceSecretKey());
  }
  
  @Test
  void loadsConfigFromSystemPropertiesWithDefaults() {
    System.setProperty("OPENAI_API_KEY", "test-key");
    System.setProperty("RAW_FOLDER_ID", "raw-folder");
    System.setProperty("EDITS_FOLDER_ID", "edits-folder");
    System.setProperty("SERVICE_PUBLIC_ID", "test-public");
    System.setProperty("SERVICE_SECRET_KEY", "test-secret");
    // Don't set OPENAI_MODEL to test default value
    System.clearProperty("OPENAI_MODEL");
    
    Config cfg = Config.loadFromSystemProperties();
    assertEquals("test-key", cfg.openaiKey());
    assertEquals("gpt-4o-mini", cfg.openaiModel()); // Should use default
    assertEquals("raw-folder", cfg.rawFolderId());
    assertEquals("edits-folder", cfg.editsFolderId());
    assertEquals("test-public", cfg.servicePublicId());
    assertEquals("test-secret", cfg.serviceSecretKey());
  }
  
  @Test
  void throwsExceptionWhenRequiredSystemPropertyMissing() {
    System.clearProperty("SERVICE_PUBLIC_ID");
    System.clearProperty("SERVICE_SECRET_KEY");
    System.clearProperty("OPENAI_API_KEY");
    System.clearProperty("RAW_FOLDER_ID");
    System.clearProperty("EDITS_FOLDER_ID");
    
    assertThrows(RuntimeException.class, () -> Config.loadFromSystemProperties());
  }
  
  @Test
  void loadsXAccessTokenSecretFromNewEnvironmentVariableName() {
    System.setProperty("OPENAI_API_KEY", "test-key");
    System.setProperty("RAW_FOLDER_ID", "raw-folder");
    System.setProperty("EDITS_FOLDER_ID", "edits-folder");
    System.setProperty("SERVICE_PUBLIC_ID", "test-public");
    System.setProperty("SERVICE_SECRET_KEY", "test-secret");
    System.setProperty("X_ACCESS_TOKEN_SECRET", "test-access-secret");
    
    Config cfg = Config.loadFromSystemProperties();
    assertEquals("test-access-secret", cfg.twAccessSecret());
  }
  
  @Test
  void loadsConfigFromEnvironmentVariablesMap() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "env-test-key");
    envVars.put("RAW_FOLDER_ID", "env-raw-folder");
    envVars.put("EDITS_FOLDER_ID", "env-edits-folder");
    envVars.put("SERVICE_PUBLIC_ID", "env-test-public");
    envVars.put("SERVICE_SECRET_KEY", "env-test-secret");
    envVars.put("X_ACCESS_TOKEN_SECRET", "env-access-secret");
    
    Config cfg = Config.loadFromEnv(envVars);
    assertEquals("env-test-key", cfg.openaiKey());
    assertEquals("gpt-4o-mini", cfg.openaiModel()); // Should use default
    assertEquals("env-raw-folder", cfg.rawFolderId());
    assertEquals("env-edits-folder", cfg.editsFolderId());
    assertEquals("env-test-public", cfg.servicePublicId());
    assertEquals("env-test-secret", cfg.serviceSecretKey());
    assertEquals("env-access-secret", cfg.twAccessSecret());
  }
  
  @Test
  void loadsConfigFromEnvironmentVariablesWithDefaults() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "test-key");
    envVars.put("RAW_FOLDER_ID", "raw");
    envVars.put("EDITS_FOLDER_ID", "edits");
    envVars.put("SERVICE_PUBLIC_ID", "public");
    envVars.put("SERVICE_SECRET_KEY", "secret");
    envVars.put("OPENAI_MODEL", "gpt-3.5-turbo");
    envVars.put("WEBHOOK_URL", "https://example.com/webhook");
    
    Config cfg = Config.loadFromEnv(envVars);
    assertEquals("test-key", cfg.openaiKey());
    assertEquals("gpt-3.5-turbo", cfg.openaiModel());
    assertEquals("raw", cfg.rawFolderId());
    assertEquals("edits", cfg.editsFolderId());
    assertEquals("https://example.com/webhook", cfg.webhookUrl());
    assertEquals("public", cfg.servicePublicId());
    assertEquals("secret", cfg.serviceSecretKey());
  }
  
  @Test
  void throwsExceptionWhenRequiredEnvironmentVariableMissing() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "test-key");
    // Missing required RAW_FOLDER_ID
    
    assertThrows(RuntimeException.class, () -> Config.loadFromEnv(envVars));
  }
  
  @Test
  void handlesBlankEnvironmentVariablesAsEmpty() {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("OPENAI_API_KEY", "test-key");
    envVars.put("RAW_FOLDER_ID", "raw");
    envVars.put("EDITS_FOLDER_ID", "edits");
    envVars.put("SERVICE_PUBLIC_ID", "public");
    envVars.put("SERVICE_SECRET_KEY", "secret");
    envVars.put("WEBHOOK_URL", "");  // Blank should use default
    envVars.put("OPENAI_MODEL", "   ");  // Whitespace should use default
    
    Config cfg = Config.loadFromEnv(envVars);
    assertEquals("", cfg.webhookUrl());  // Blank becomes empty string
    assertEquals("gpt-4o-mini", cfg.openaiModel());  // Whitespace uses default
  }
}