package com.autopost.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Redactor utility to ensure sensitive information is properly masked in logs and
 * exception messages.
 */
class RedactorTest {

  private Redactor redactor;

  @BeforeEach
  void setUp() {
    redactor = new Redactor();
  }

  @Test
  void shouldRedactApiKeys() {
    // Given: Messages containing API keys
    String message1 = "api_key=sk-1234567890abcdef";
    String message2 = "API_KEY: Bearer sk-abcdef1234567890";
    String message3 = "token=\"xoxb-1234-5678-abcdef\"";

    // When: Redacting the messages
    String result1 = redactor.redact(message1);
    String result2 = redactor.redact(message2);
    String result3 = redactor.redact(message3);

    // Then: Should mask the sensitive values
    assertTrue(result1.contains("[REDACTED]"));
    assertFalse(result1.contains("sk-1234567890abcdef"));

    assertTrue(result2.contains("[REDACTED]"));
    assertFalse(result2.contains("sk-abcdef1234567890"));

    assertTrue(result3.contains("[REDACTED]"));
    assertFalse(result3.contains("xoxb-1234-5678-abcdef"));
  }

  @Test
  void shouldRedactBearerTokens() {
    // Given: Messages containing Bearer tokens
    String message = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

    // When: Redacting the message
    String result = redactor.redact(message);

    // Then: Should mask the token
    assertTrue(result.contains("Bearer [REDACTED]"));
    assertFalse(result.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));
  }

  @Test
  void shouldRedactJwtTokens() {
    // Given: Message containing JWT token
    String jwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    String message = "Token: " + jwt;

    // When: Redacting the message
    String result = redactor.redact(message);

    // Then: Should mask the JWT
    assertTrue(result.contains("[REDACTED]"));
    assertFalse(result.contains(jwt));
  }

  @Test
  void shouldRedactGooglePrivateKeys() {
    // Given: Message containing Google private key
    String message =
        "private_key: \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC\"";

    // When: Redacting the message
    String result = redactor.redact(message);

    // Then: Should mask the private key
    assertTrue(result.contains("[REDACTED]"));
    assertFalse(result.contains("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC"));
  }

  @Test
  void shouldRedactOAuthTokens() {
    // Given: Messages containing OAuth tokens
    String message1 = "oauth_token=1234567890abcdef";
    String message2 = "access_token: xoxb-1234567890";

    // When: Redacting the messages
    String result1 = redactor.redact(message1);
    String result2 = redactor.redact(message2);

    // Then: Should mask the tokens
    assertTrue(result1.contains("[REDACTED]"));
    assertFalse(result1.contains("1234567890abcdef"));

    assertTrue(result2.contains("[REDACTED]"));
    assertFalse(result2.contains("xoxb-1234567890"));
  }

  @Test
  void shouldNotRedactNormalText() {
    // Given: Normal non-sensitive message
    String message = "User logged in successfully. Processing video file.";

    // When: Redacting the message
    String result = redactor.redact(message);

    // Then: Should remain unchanged
    assertEquals(message, result);
    assertFalse(result.contains("[REDACTED]"));
  }

  @Test
  void shouldHandleNullAndEmptyMessages() {
    // Given: Null and empty messages

    // When/Then: Should handle gracefully
    assertNull(redactor.redact(null));
    assertEquals("", redactor.redact(""));
    assertEquals("   ", redactor.redact("   "));
  }

  @Test
  void shouldRedactExceptionChain() {
    // Given: Exception chain with sensitive information
    Exception cause = new RuntimeException("Connection failed: api_key=sk-1234567890");
    Exception wrapper = new RuntimeException("Request failed", cause);

    // When: Redacting exception
    String result = redactor.redactException(wrapper);

    // Then: Should redact sensitive info in the chain
    assertTrue(result.contains("[REDACTED]"));
    assertFalse(result.contains("sk-1234567890"));
    assertTrue(result.contains("RuntimeException"));
    assertTrue(result.contains("Caused by"));
  }

  @Test
  void shouldHandleNullException() {
    // Given: Null exception

    // When/Then: Should handle gracefully
    assertNull(redactor.redactException(null));
  }

  @Test
  void shouldRedactSensitiveHeaders() {
    // Given: Various headers
    String authHeader = "Bearer sk-1234567890";
    String normalHeader = "application/json";

    // When: Redacting headers
    String redactedAuth = redactor.redactHeader("Authorization", authHeader);
    String redactedNormal = redactor.redactHeader("Content-Type", normalHeader);

    // Then: Should redact sensitive headers only
    assertEquals("[REDACTED]", redactedAuth);
    assertEquals("application/json", redactedNormal);
  }

  @Test
  void shouldDetectSensitiveInformation() {
    // Given: Various messages
    String sensitiveMessage = "api_key=sk-1234567890";
    String normalMessage = "Hello world";

    // When: Checking for sensitive info
    boolean hasSensitive = redactor.containsSensitiveInfo(sensitiveMessage);
    boolean hasNormal = redactor.containsSensitiveInfo(normalMessage);

    // Then: Should correctly identify sensitive content
    assertTrue(hasSensitive);
    assertFalse(hasNormal);
  }

  @Test
  void shouldRedactObjectToString() {
    // Given: Object that might contain sensitive info in toString
    Object obj =
        new Object() {
          @Override
          public String toString() {
            return "Config{apiKey=sk-1234567890, url=https://api.example.com}";
          }
        };

    // When: Redacting object
    String result = redactor.redactObject(obj);

    // Then: Should redact sensitive parts
    assertTrue(
        result.contains("[REDACTED]"), "Should contain redacted placeholder, got: " + result);
    assertFalse(result.contains("sk-1234567890"), "Should not contain original API key");
    // Note: URL might get redacted too, which is acceptable for security
  }

  @Test
  void shouldHandleNullObject() {
    // Given: Null object

    // When/Then: Should handle gracefully
    assertEquals("null", redactor.redactObject(null));
  }
}
