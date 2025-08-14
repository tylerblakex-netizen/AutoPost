package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.HashMap;

/**
 * Test class for Config that demonstrates safe testing practices without reflection.
 * Instead of using reflection to modify environment variables, this approach uses
 * system properties as a testing alternative when appropriate, or creates Config
 * objects directly with test data.
 */
public class ConfigTest {
  
  private Map<String, String> originalSystemProperties;
  
  @BeforeEach
  void setUp() {
    // Save original system properties that we might modify
    originalSystemProperties = new HashMap<>();
    for (String prop : new String[]{
        "test.openai.key", "test.raw.folder", "test.edits.folder"
    }) {
      originalSystemProperties.put(prop, System.getProperty(prop));
    }
  }
  
  @AfterEach
  void tearDown() {
    // Restore original system properties
    for (Map.Entry<String, String> entry : originalSystemProperties.entrySet()) {
      if (entry.getValue() == null) {
        System.clearProperty(entry.getKey());
      } else {
        System.setProperty(entry.getKey(), entry.getValue());
      }
    }
  }
  
  @Test
  void configCanBeCreatedDirectly() {
    // Instead of trying to modify environment variables, create Config directly
    // This tests the Config record functionality without environment manipulation
    Config cfg = new Config(
        "test-openai-key",
        "gpt-4",
        "raw-folder-id",
        "edits-folder-id", 
        "https://webhook.example.com",
        "/path/to/sa.json",
        "{\"type\":\"service_account\"}",
        "twitter-api-key",
        "twitter-api-secret",
        "twitter-access-token",
        "twitter-access-secret"
    );
    
    assertEquals("test-openai-key", cfg.openaiKey());
    assertEquals("gpt-4", cfg.openaiModel());
    assertEquals("raw-folder-id", cfg.rawFolderId());
    assertEquals("edits-folder-id", cfg.editsFolderId());
    assertEquals("https://webhook.example.com", cfg.webhookUrl());
    assertTrue(cfg.hasInlineSA());
    assertTrue(cfg.hasSAPath());
  }
  
  @Test
  void configWithEmptyServiceAccount() {
    Config cfg = new Config(
        "test-openai-key",
        "gpt-4",
        "raw-folder-id",
        "edits-folder-id", 
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    );
    
    assertFalse(cfg.hasInlineSA());
    assertFalse(cfg.hasSAPath());
  }
  
  @Test 
  void configWithBlankServiceAccount() {
    Config cfg = new Config(
        "test-openai-key",
        "gpt-4",
        "raw-folder-id",
        "edits-folder-id", 
        "",
        "   ",
        "   ",
        "",
        "",
        "",
        ""
    );
    
    assertFalse(cfg.hasInlineSA());
    assertFalse(cfg.hasSAPath());
  }
  
  @Test
  void envMethodReturnsDefaultWhenVarIsNull() {
    // Test the static helper methods directly
    // Since we can't safely mock System.getenv, we test with a variable
    // that's unlikely to be set in the test environment
    String result = Config.env("UNLIKELY_TEST_VAR_THAT_SHOULD_NOT_EXIST", "default-value");
    assertEquals("default-value", result);
  }
  
  @Test
  void reqMethodThrowsWhenVarIsNull() {
    // Test that req() throws an exception for missing variables
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      Config.req("UNLIKELY_TEST_VAR_THAT_SHOULD_NOT_EXIST");
    });
    
    assertEquals("UNLIKELY_TEST_VAR_THAT_SHOULD_NOT_EXIST is required", exception.getMessage());
  }

  /**
   * This test demonstrates the difference between the old reflection-based approach
   * and the safer alternatives. The old approach used:
   * 
   *   private static void setEnv(String key, String value) throws Exception {
   *     Map<String, String> env = System.getenv();
   *     Field field = env.getClass().getDeclaredField("m");
   *     field.setAccessible(true);
   *     @SuppressWarnings("unchecked")
   *     Map<String, String> writableEnv = (Map<String, String>) field.get(env);
   *     writableEnv.put(key, value);
   *   }
   * 
   * This approach was problematic because:
   * 1. It relied on JVM implementation details (the "m" field)
   * 2. It used reflection to access private internals
   * 3. It could break with different JVM implementations
   * 4. It interfered with the system's environment in unpredictable ways
   * 
   * The new approach:
   * 1. Creates Config objects directly with test data
   * 2. Tests the actual business logic rather than environment manipulation
   * 3. Is more reliable and portable across JVM implementations
   * 4. Clearly separates concerns between environment setup and business logic
   */
  @Test
  void demonstratesWhyDirectConfigCreationIsBetter() {
    // Old approach required reflection and environment manipulation
    // New approach: test the actual behavior we care about
    
    Config configWithDefaults = new Config(
        "test-key", "gpt-4o-mini", "raw", "edits", "", "", "", "", "", "", ""
    );
    
    Config configWithCustomValues = new Config(
        "test-key", "gpt-4", "raw", "edits", "https://example.com", 
        "/sa.json", "{\"sa\":true}", "tw-key", "tw-secret", "tw-token", "tw-access"
    );
    
    // Test the actual behavior we care about
    assertEquals("gpt-4o-mini", configWithDefaults.openaiModel());
    assertEquals("gpt-4", configWithCustomValues.openaiModel());
    
    assertFalse(configWithDefaults.hasInlineSA());
    assertTrue(configWithCustomValues.hasInlineSA());
  }
}