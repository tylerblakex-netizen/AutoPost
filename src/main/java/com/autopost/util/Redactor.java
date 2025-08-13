package com.autopost.util;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Utility class for redacting sensitive information from logs and exception messages. This helps
 * prevent accidental exposure of secrets in application logs.
 */
@Component
public class Redactor {

  // Patterns to detect token-like strings
  private static final Pattern API_KEY_PATTERN =
      Pattern.compile(
          "(?i)(api[_-]?key|token|secret|password|credential)[\"'\\s]*[:=][\"'\\s]*([a-zA-Z0-9+/=_-]{8,})",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern BEARER_TOKEN_PATTERN =
      Pattern.compile("(?i)bearer\\s+([a-zA-Z0-9+/=_-]{8,})", Pattern.CASE_INSENSITIVE);

  private static final Pattern JSON_WEB_TOKEN_PATTERN =
      Pattern.compile("\\b[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");

  private static final Pattern OAUTH_TOKEN_PATTERN =
      Pattern.compile(
          "(?i)(oauth[_-]?token|access[_-]?token)[\"'\\s]*[:=][\"'\\s]*([a-zA-Z0-9+/=_-]{8,})",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern GOOGLE_KEY_PATTERN =
      Pattern.compile(
          "(?i)(private_key[\"'\\s]*[:=][\"'\\s]*[\"'])([^\"']+)([\"'])", Pattern.CASE_INSENSITIVE);

  // Enhanced pattern for object toString that might contain API keys
  private static final Pattern API_KEY_IN_OBJECT_PATTERN =
      Pattern.compile(
          "(?i)(apikey|api_key|token|key)\\s*[=:]\\s*([a-zA-Z0-9+/=_-]{8,})",
          Pattern.CASE_INSENSITIVE);

  private static final String REDACTED = "[REDACTED]";

  /**
   * Redacts sensitive information from a message string.
   *
   * @param message the message to redact
   * @return the message with sensitive information replaced
   */
  public String redact(String message) {
    if (message == null || message.isEmpty()) {
      return message;
    }

    String redacted = message;

    // Redact API keys and tokens
    redacted = API_KEY_PATTERN.matcher(redacted).replaceAll("$1:" + REDACTED);

    // Redact Bearer tokens
    redacted = BEARER_TOKEN_PATTERN.matcher(redacted).replaceAll("Bearer " + REDACTED);

    // Redact JWT tokens
    redacted = JSON_WEB_TOKEN_PATTERN.matcher(redacted).replaceAll(REDACTED);

    // Redact OAuth tokens
    redacted = OAUTH_TOKEN_PATTERN.matcher(redacted).replaceAll("$1:" + REDACTED);

    // Redact Google private keys
    redacted = GOOGLE_KEY_PATTERN.matcher(redacted).replaceAll("$1" + REDACTED + "$3");

    // Redact API keys in object toString format
    redacted = API_KEY_IN_OBJECT_PATTERN.matcher(redacted).replaceAll("$1=" + REDACTED);

    return redacted;
  }

  /**
   * Redacts sensitive information from an exception message and its cause chain.
   *
   * @param throwable the exception to redact
   * @return the exception message with sensitive information replaced
   */
  public String redactException(Throwable throwable) {
    if (throwable == null) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    Throwable current = throwable;

    while (current != null) {
      if (result.length() > 0) {
        result.append(" Caused by: ");
      }
      result.append(current.getClass().getSimpleName());
      if (current.getMessage() != null) {
        result.append(": ").append(redact(current.getMessage()));
      }
      current = current.getCause();
    }

    return result.toString();
  }

  /**
   * Creates a safe string representation of an object, redacting any sensitive fields.
   *
   * @param obj the object to create a string representation of
   * @return a string representation with sensitive information redacted
   */
  public String redactObject(Object obj) {
    if (obj == null) {
      return "null";
    }

    return redact(obj.toString());
  }

  /**
   * Redacts sensitive headers from HTTP requests/responses.
   *
   * @param headerName the header name
   * @param headerValue the header value
   * @return the redacted header value if sensitive, otherwise the original value
   */
  public String redactHeader(String headerName, String headerValue) {
    if (headerName == null || headerValue == null) {
      return headerValue;
    }

    String lowerName = headerName.toLowerCase();
    if (lowerName.contains("authorization")
        || lowerName.contains("token")
        || lowerName.contains("key")
        || lowerName.contains("secret")
        || lowerName.contains("credential")) {
      return REDACTED;
    }

    return headerValue;
  }

  /**
   * Checks if a string contains potentially sensitive information.
   *
   * @param text the text to check
   * @return true if the text might contain sensitive information
   */
  public boolean containsSensitiveInfo(String text) {
    if (text == null || text.isEmpty()) {
      return false;
    }

    return API_KEY_PATTERN.matcher(text).find()
        || BEARER_TOKEN_PATTERN.matcher(text).find()
        || JSON_WEB_TOKEN_PATTERN.matcher(text).find()
        || OAUTH_TOKEN_PATTERN.matcher(text).find()
        || GOOGLE_KEY_PATTERN.matcher(text).find()
        || API_KEY_IN_OBJECT_PATTERN.matcher(text).find();
  }
}
