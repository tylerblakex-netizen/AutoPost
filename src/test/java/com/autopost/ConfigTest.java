package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
  void loadsAllXVariablesFromNewNamingConvention() {
    System.setProperty("OPENAI_API_KEY", "test-key");
    System.setProperty("RAW_FOLDER_ID", "raw-folder");
    System.setProperty("EDITS_FOLDER_ID", "edits-folder");
    System.setProperty("SERVICE_PUBLIC_ID", "test-public");
    System.setProperty("SERVICE_SECRET_KEY", "test-secret");
    System.setProperty("X_API_KEY", "test-api-key");
    System.setProperty("X_API_SECRET", "test-api-secret");
    System.setProperty("X_ACCESS_TOKEN", "test-access-token");
    System.setProperty("X_ACCESS_TOKEN_SECRET", "test-access-secret");
    
    Config cfg = Config.loadFromSystemProperties();
    assertEquals("test-api-key", cfg.twApiKey());
    assertEquals("test-api-secret", cfg.twApiSecret());
    assertEquals("test-access-token", cfg.twAccessToken());
    assertEquals("test-access-secret", cfg.twAccessSecret());
  }
}