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
}