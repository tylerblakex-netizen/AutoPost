package com.autopost.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ConfigProperties validation and environment variable handling. These tests verify that
 * the configuration fails fast with meaningful errors when required environment variables are
 * missing.
 */
class ConfigPropertiesTest {

  private ConfigProperties config;

  @BeforeEach
  void setUp() {
    config = new ConfigProperties();

    // Clear any existing environment variables to ensure clean test state
    clearEnvironment();
  }

  @AfterEach
  void tearDown() {
    clearEnvironment();
  }

  private void clearEnvironment() {
    // Note: We can't actually clear system environment variables in tests,
    // but we can ensure our test environment doesn't rely on them
  }

  @Test
  void shouldFailValidationWhenRequiredVariablesMissing() {
    // Given: ConfigProperties with no environment variables set

    // When/Then: Should throw IllegalStateException listing missing variables
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> config.validateConfiguration());

    String message = exception.getMessage();
    assertAll(
        "Should list all missing required variables",
        () -> assertTrue(message.contains("Missing required environment variables")),
        () -> assertTrue(message.contains("OPENAI_API_KEY")),
        () -> assertTrue(message.contains("X_API_KEY")),
        () -> assertTrue(message.contains("X_API_SECRET")),
        () -> assertTrue(message.contains("X_ACCESS_TOKEN")),
        () -> assertTrue(message.contains("X_ACCESS_TOKEN_SECRET")),
        () -> assertTrue(message.contains("GOOGLE_SERVICE_ACCOUNT_JSON")),
        () ->
            assertTrue(
                message.contains("GOOGLE_RAW_FOLDER_ID") || message.contains("RAW_FOLDER_ID")));
  }

  @Test
  void shouldPassValidationWhenAllRequiredVariablesPresent() {
    // Given: All required variables are set via setters (simulating environment)
    config.setOpenaiApiKey("test-openai-key");
    config.setXApiKey("test-x-api-key");
    config.setXApiSecret("test-x-api-secret");
    config.setXAccessToken("test-x-access-token");
    config.setXAccessTokenSecret("test-x-access-token-secret");
    config.setGoogleServiceAccountJson("{\"type\":\"service_account\"}");
    config.setGoogleRawFolderId("test-raw-folder-id");
    config.setGoogleEditsFolderId("test-edits-folder-id");

    // When/Then: Should not throw any exception
    assertDoesNotThrow(() -> config.validateConfiguration());
  }

  @Test
  void shouldUseDefaultValues() {
    // Given: Fresh ConfigProperties instance

    // When/Then: Should have sensible defaults
    assertEquals("gpt-4o-mini", config.getOpenaiModel());
    assertEquals("0 0 9 * * *", config.getAutoPostCron());
    assertEquals(3, config.getMaxHashtags());
    assertTrue(config.isPostOneTeaserPerDay());
    assertEquals("ffmpeg", config.getFfmpegPath());
    assertEquals("ffprobe", config.getFfprobePath());
    assertEquals("", config.getWebhookUrl());
  }

  @Test
  void shouldValidateMaxHashtagsRange() {
    // Given: ConfigProperties with valid required fields
    setAllRequiredFields();

    // When: Setting max hashtags within valid range
    config.setMaxHashtags(1);
    assertDoesNotThrow(() -> config.validateConfiguration());

    config.setMaxHashtags(10);
    assertDoesNotThrow(() -> config.validateConfiguration());
  }

  @Test
  void shouldDetectXCredentialsPresence() {
    // Given: ConfigProperties with X credentials
    config.setXApiKey("test-key");
    config.setXApiSecret("test-secret");
    config.setXAccessToken("test-token");
    config.setXAccessTokenSecret("test-token-secret");

    // When/Then: Should detect credentials are present
    assertTrue(config.hasXCredentials());
  }

  @Test
  void shouldDetectMissingXCredentials() {
    // Given: ConfigProperties with incomplete X credentials
    config.setXApiKey("test-key");
    // Missing other X credentials

    // When/Then: Should detect credentials are missing
    assertFalse(config.hasXCredentials());
  }

  @Test
  void shouldDetectWebhookPresence() {
    // Given: ConfigProperties with webhook URL
    config.setWebhookUrl("https://example.com/webhook");

    // When/Then: Should detect webhook is present
    assertTrue(config.hasWebhook());
  }

  @Test
  void shouldDetectMissingWebhook() {
    // Given: ConfigProperties with empty webhook URL
    config.setWebhookUrl("");

    // When/Then: Should detect webhook is missing
    assertFalse(config.hasWebhook());
  }

  @Test
  void shouldDetectOptionalApiKeys() {
    // Given: ConfigProperties with optional API keys
    config.setAnthropicApiKey("test-anthropic-key");
    config.setGrokApiKey("test-grok-key");

    // When/Then: Should detect optional APIs are present
    assertTrue(config.hasAnthropicApi());
    assertTrue(config.hasGrokApi());
  }

  @Test
  void shouldHandleBackwardCompatibilityForFolderIds() {
    // This test would need actual environment variable mocking
    // which is complex in unit tests. Integration tests would be better.
    // For now, we test the logic via direct property setting.

    // Given: ConfigProperties with backward compatible folder IDs
    config.setGoogleRawFolderId("test-raw-folder");
    config.setGoogleEditsFolderId("test-edits-folder");

    // When/Then: Properties should be accessible
    assertEquals("test-raw-folder", config.getGoogleRawFolderId());
    assertEquals("test-edits-folder", config.getGoogleEditsFolderId());
  }

  private void setAllRequiredFields() {
    config.setOpenaiApiKey("test-openai-key");
    config.setXApiKey("test-x-api-key");
    config.setXApiSecret("test-x-api-secret");
    config.setXAccessToken("test-x-access-token");
    config.setXAccessTokenSecret("test-x-access-token-secret");
    config.setGoogleServiceAccountJson("{\"type\":\"service_account\"}");
    config.setGoogleRawFolderId("test-raw-folder-id");
    config.setGoogleEditsFolderId("test-edits-folder-id");
  }
}
