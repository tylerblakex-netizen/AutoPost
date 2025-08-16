package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class ConfigTest {
  
  @BeforeEach
  void clearEnvironment() {
    // Clear all relevant environment properties before each test
    System.clearProperty("GOOGLE_SERVICE_ACCOUNT_JSON");
    System.clearProperty("GOOGLE_APPLICATION_CREDENTIALS");
  }
  
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
    assertTrue(cfg.serviceEnabled());
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
    assertTrue(cfg.serviceEnabled());
  }
  
  @Test
  void throwsExceptionWhenRequiredSystemPropertyMissing() {
    System.clearProperty("OPENAI_API_KEY");
    System.clearProperty("RAW_FOLDER_ID");
    System.clearProperty("EDITS_FOLDER_ID");
    
    assertThrows(RuntimeException.class, () -> Config.loadFromSystemProperties());
  }

  @Test
  void serviceCredentialsOptionalWhenNotProvided() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.clearProperty("SERVICE_PUBLIC_ID");
    System.clearProperty("SERVICE_SECRET_KEY");

    Config cfg = Config.loadFromSystemProperties();
    assertEquals("", cfg.servicePublicId());
    assertEquals("", cfg.serviceSecretKey());
    assertFalse(cfg.serviceEnabled());
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
  void hasInlineSAReturnsFalseWhenJsonIsEmpty() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    // Don't set GOOGLE_SERVICE_ACCOUNT_JSON - should default to empty
    
    Config cfg = Config.loadFromSystemProperties();
    assertFalse(cfg.hasInlineSA());
  }
  
  @Test
  void hasInlineSAReturnsFalseWhenJsonIsBlank() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    System.setProperty("GOOGLE_SERVICE_ACCOUNT_JSON", "   ");
    
    Config cfg = Config.loadFromSystemProperties();
    assertFalse(cfg.hasInlineSA());
  }
  
  @Test
  void hasInlineSAThrowsExceptionWhenJsonIsInvalid() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    System.setProperty("GOOGLE_SERVICE_ACCOUNT_JSON", "invalid json content");
    
    Config cfg = Config.loadFromSystemProperties();
    RuntimeException exception = assertThrows(RuntimeException.class, cfg::hasInlineSA);
    assertEquals("GOOGLE_SERVICE_ACCOUNT_JSON contains invalid JSON format", exception.getMessage());
  }
  
  @Test
  void hasInlineSAThrowsExceptionWhenJsonIsMalformed() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    System.setProperty("GOOGLE_SERVICE_ACCOUNT_JSON", "{\"key\": \"value\"");  // missing closing brace
    
    Config cfg = Config.loadFromSystemProperties();
    RuntimeException exception = assertThrows(RuntimeException.class, cfg::hasInlineSA);
    assertEquals("GOOGLE_SERVICE_ACCOUNT_JSON contains invalid JSON format", exception.getMessage());
  }
  
  @Test
  void hasInlineSAReturnsTrueWhenJsonIsValid() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    System.setProperty("GOOGLE_SERVICE_ACCOUNT_JSON", "{\"type\": \"service_account\", \"project_id\": \"test\"}");
    
    Config cfg = Config.loadFromSystemProperties();
    assertTrue(cfg.hasInlineSA());
  }
  
  @Test
  void hasInlineSAReturnsTrueWithComplexValidJson() {
    System.setProperty("OPENAI_API_KEY", "test");
    System.setProperty("RAW_FOLDER_ID", "raw");
    System.setProperty("EDITS_FOLDER_ID", "edits");
    System.setProperty("SERVICE_PUBLIC_ID", "public");
    System.setProperty("SERVICE_SECRET_KEY", "secret");
    // Simulated Google service account JSON structure
    String validJson = "{"
        + "\"type\": \"service_account\","
        + "\"project_id\": \"test-project\","
        + "\"private_key_id\": \"key-id\","
        + "\"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BA\\n-----END PRIVATE KEY-----\\n\","
        + "\"client_email\": \"test@test-project.iam.gserviceaccount.com\","
        + "\"client_id\": \"123456789\","
        + "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\","
        + "\"token_uri\": \"https://oauth2.googleapis.com/token\""
        + "}";
    System.setProperty("GOOGLE_SERVICE_ACCOUNT_JSON", validJson);
    
    Config cfg = Config.loadFromSystemProperties();
    assertTrue(cfg.hasInlineSA());
  }
  
  @Test
  void hasInlineSAWorksWithEnvironmentVariables() {
    // Test that it also works with environment variables via loadFromEnv()
    // We can't set actual environment variables in tests, but we can test 
    // that the Config object behaves correctly when saInlineJson is set
    Config cfg = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", 
        "{\"type\": \"service_account\"}", // valid JSON
        "", "", "", "", "public", "secret"
    );
    assertTrue(cfg.hasInlineSA());
    
    Config cfg2 = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", 
        "invalid json", // invalid JSON
        "", "", "", "", "public", "secret"
    );
    RuntimeException exception = assertThrows(RuntimeException.class, cfg2::hasInlineSA);
    assertEquals("GOOGLE_SERVICE_ACCOUNT_JSON contains invalid JSON format", exception.getMessage());
  }
}