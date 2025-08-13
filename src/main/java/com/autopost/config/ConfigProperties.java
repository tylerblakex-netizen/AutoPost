package com.autopost.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Centralized configuration properties for AutoPost application. All environment variables are
 * mapped here with validation and fail-fast behavior.
 */
@Component
@ConfigurationProperties(prefix = "autopost")
@Validated
public class ConfigProperties {

  private static final Logger log = LoggerFactory.getLogger(ConfigProperties.class);

  // OpenAI Configuration
  private String openaiApiKey;
  private String anthropicApiKey; // Optional
  private String grokApiKey; // Optional
  private String openaiModel = "gpt-4o-mini"; // Default

  // X/Twitter Configuration
  private String xBearerToken; // Optional
  private String xApiKey;
  private String xApiSecret;
  private String xAccessToken;
  private String xAccessTokenSecret;

  // Google Drive Configuration
  private String googleClientId; // Optional
  private String googleClientSecret; // Optional
  private String googleServiceAccountJson; // Inline JSON
  private String googleRawFolderId;
  private String googleEditsFolderId;

  // Scheduling Configuration
  @NotBlank private String autoPostCron = "0 0 9 * * *"; // Default 09:00 UTC

  @Min(1)
  @Max(10)
  private int maxHashtags = 3;

  private boolean postOneTeaserPerDay = true;

  // FFmpeg Configuration
  private String ffmpegPath = "ffmpeg";
  private String ffprobePath = "ffprobe";

  // Webhook Configuration
  private String webhookUrl = "";

  @PostConstruct
  public void validateConfiguration() {
    List<String> missingRequired = new ArrayList<>();

    // Check required environment variables
    if (isBlank(getEnvOrProperty("OPENAI_API_KEY", openaiApiKey))) {
      missingRequired.add("OPENAI_API_KEY");
    }

    if (isBlank(getEnvOrProperty("X_API_KEY", xApiKey))) {
      missingRequired.add("X_API_KEY");
    }

    if (isBlank(getEnvOrProperty("X_API_SECRET", xApiSecret))) {
      missingRequired.add("X_API_SECRET");
    }

    if (isBlank(getEnvOrProperty("X_ACCESS_TOKEN", xAccessToken))) {
      missingRequired.add("X_ACCESS_TOKEN");
    }

    if (isBlank(getEnvOrProperty("X_ACCESS_TOKEN_SECRET", xAccessTokenSecret))) {
      missingRequired.add("X_ACCESS_TOKEN_SECRET");
    }

    if (isBlank(getEnvOrProperty("GOOGLE_SERVICE_ACCOUNT_JSON", googleServiceAccountJson))) {
      missingRequired.add("GOOGLE_SERVICE_ACCOUNT_JSON");
    }

    if (isBlank(
        getEnvOrProperty(
            "GOOGLE_RAW_FOLDER_ID", getEnvOrProperty("RAW_FOLDER_ID", googleRawFolderId)))) {
      missingRequired.add("GOOGLE_RAW_FOLDER_ID (or RAW_FOLDER_ID for backward compatibility)");
    }

    if (isBlank(
        getEnvOrProperty(
            "GOOGLE_EDITS_FOLDER_ID", getEnvOrProperty("EDITS_FOLDER_ID", googleEditsFolderId)))) {
      missingRequired.add("GOOGLE_EDITS_FOLDER_ID (or EDITS_FOLDER_ID for backward compatibility)");
    }

    // Load values from environment with backward compatibility
    loadFromEnvironment();

    if (!missingRequired.isEmpty()) {
      String message =
          "Missing required environment variables: " + String.join(", ", missingRequired);
      log.error("Configuration validation failed: {}", message);
      throw new IllegalStateException(message);
    }

    log.info("✅ Configuration validation passed - all required environment variables present");
    log.info("✅ Scheduling cron: {}", autoPostCron);
    log.info("✅ Max hashtags: {}", maxHashtags);
    log.info("✅ One teaser per day: {}", postOneTeaserPerDay);
  }

  private void loadFromEnvironment() {
    // Required OpenAI
    openaiApiKey = getEnvOrProperty("OPENAI_API_KEY", openaiApiKey);
    openaiModel = getEnvOrProperty("OPENAI_MODEL", openaiModel);

    // Optional AI APIs
    anthropicApiKey = getEnvOrProperty("ANTHROPIC_API_KEY", anthropicApiKey);
    grokApiKey = getEnvOrProperty("GROK_API_KEY", grokApiKey);

    // X/Twitter (required for posting)
    xBearerToken = getEnvOrProperty("X_BEARER_TOKEN", xBearerToken);
    xApiKey = getEnvOrProperty("X_API_KEY", xApiKey);
    xApiSecret = getEnvOrProperty("X_API_SECRET", xApiSecret);
    xAccessToken = getEnvOrProperty("X_ACCESS_TOKEN", xAccessToken);
    xAccessTokenSecret = getEnvOrProperty("X_ACCESS_TOKEN_SECRET", xAccessTokenSecret);

    // Google Drive (required)
    googleClientId = getEnvOrProperty("GOOGLE_CLIENT_ID", googleClientId);
    googleClientSecret = getEnvOrProperty("GOOGLE_CLIENT_SECRET", googleClientSecret);
    googleServiceAccountJson =
        getEnvOrProperty("GOOGLE_SERVICE_ACCOUNT_JSON", googleServiceAccountJson);

    // Support backward compatibility for folder IDs
    googleRawFolderId =
        getEnvOrProperty(
            "GOOGLE_RAW_FOLDER_ID", getEnvOrProperty("RAW_FOLDER_ID", googleRawFolderId));
    googleEditsFolderId =
        getEnvOrProperty(
            "GOOGLE_EDITS_FOLDER_ID", getEnvOrProperty("EDITS_FOLDER_ID", googleEditsFolderId));

    // Scheduling
    autoPostCron = getEnvOrProperty("AUTO_POST_CRON", autoPostCron);
    String maxHashtagsStr = getEnvOrProperty("MAX_HASHTAGS", String.valueOf(maxHashtags));
    try {
      maxHashtags = Integer.parseInt(maxHashtagsStr);
    } catch (NumberFormatException e) {
      log.warn("Invalid MAX_HASHTAGS value: {}, using default: {}", maxHashtagsStr, maxHashtags);
    }

    String postOneTeaserStr =
        getEnvOrProperty("POST_ONE_TEASER_PER_DAY", String.valueOf(postOneTeaserPerDay));
    postOneTeaserPerDay = Boolean.parseBoolean(postOneTeaserStr);

    // FFmpeg paths
    ffmpegPath = getEnvOrProperty("FFMPEG_PATH", ffmpegPath);
    ffprobePath = getEnvOrProperty("FFPROBE_PATH", ffprobePath);

    // Webhook
    webhookUrl = getEnvOrProperty("WEBHOOK_URL", webhookUrl);
  }

  private String getEnvOrProperty(String envName, String defaultValue) {
    String envValue = System.getenv(envName);
    if (envValue != null && !envValue.trim().isEmpty()) {
      return envValue.trim();
    }
    return defaultValue;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  // Getters and setters for all properties

  public String getOpenaiApiKey() {
    return openaiApiKey;
  }

  public void setOpenaiApiKey(String openaiApiKey) {
    this.openaiApiKey = openaiApiKey;
  }

  public String getAnthropicApiKey() {
    return anthropicApiKey;
  }

  public void setAnthropicApiKey(String anthropicApiKey) {
    this.anthropicApiKey = anthropicApiKey;
  }

  public String getGrokApiKey() {
    return grokApiKey;
  }

  public void setGrokApiKey(String grokApiKey) {
    this.grokApiKey = grokApiKey;
  }

  public String getOpenaiModel() {
    return openaiModel;
  }

  public void setOpenaiModel(String openaiModel) {
    this.openaiModel = openaiModel;
  }

  public String getXBearerToken() {
    return xBearerToken;
  }

  public void setXBearerToken(String xBearerToken) {
    this.xBearerToken = xBearerToken;
  }

  public String getXApiKey() {
    return xApiKey;
  }

  public void setXApiKey(String xApiKey) {
    this.xApiKey = xApiKey;
  }

  public String getXApiSecret() {
    return xApiSecret;
  }

  public void setXApiSecret(String xApiSecret) {
    this.xApiSecret = xApiSecret;
  }

  public String getXAccessToken() {
    return xAccessToken;
  }

  public void setXAccessToken(String xAccessToken) {
    this.xAccessToken = xAccessToken;
  }

  public String getXAccessTokenSecret() {
    return xAccessTokenSecret;
  }

  public void setXAccessTokenSecret(String xAccessTokenSecret) {
    this.xAccessTokenSecret = xAccessTokenSecret;
  }

  public String getGoogleClientId() {
    return googleClientId;
  }

  public void setGoogleClientId(String googleClientId) {
    this.googleClientId = googleClientId;
  }

  public String getGoogleClientSecret() {
    return googleClientSecret;
  }

  public void setGoogleClientSecret(String googleClientSecret) {
    this.googleClientSecret = googleClientSecret;
  }

  public String getGoogleServiceAccountJson() {
    return googleServiceAccountJson;
  }

  public void setGoogleServiceAccountJson(String googleServiceAccountJson) {
    this.googleServiceAccountJson = googleServiceAccountJson;
  }

  public String getGoogleRawFolderId() {
    return googleRawFolderId;
  }

  public void setGoogleRawFolderId(String googleRawFolderId) {
    this.googleRawFolderId = googleRawFolderId;
  }

  public String getGoogleEditsFolderId() {
    return googleEditsFolderId;
  }

  public void setGoogleEditsFolderId(String googleEditsFolderId) {
    this.googleEditsFolderId = googleEditsFolderId;
  }

  public String getAutoPostCron() {
    return autoPostCron;
  }

  public void setAutoPostCron(String autoPostCron) {
    this.autoPostCron = autoPostCron;
  }

  public int getMaxHashtags() {
    return maxHashtags;
  }

  public void setMaxHashtags(int maxHashtags) {
    this.maxHashtags = maxHashtags;
  }

  public boolean isPostOneTeaserPerDay() {
    return postOneTeaserPerDay;
  }

  public void setPostOneTeaserPerDay(boolean postOneTeaserPerDay) {
    this.postOneTeaserPerDay = postOneTeaserPerDay;
  }

  public String getFfmpegPath() {
    return ffmpegPath;
  }

  public void setFfmpegPath(String ffmpegPath) {
    this.ffmpegPath = ffmpegPath;
  }

  public String getFfprobePath() {
    return ffprobePath;
  }

  public void setFfprobePath(String ffprobePath) {
    this.ffprobePath = ffprobePath;
  }

  public String getWebhookUrl() {
    return webhookUrl;
  }

  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
  }

  // Convenience methods
  public boolean hasXCredentials() {
    return !isBlank(xApiKey)
        && !isBlank(xApiSecret)
        && !isBlank(xAccessToken)
        && !isBlank(xAccessTokenSecret);
  }

  public boolean hasWebhook() {
    return !isBlank(webhookUrl);
  }

  public boolean hasAnthropicApi() {
    return !isBlank(anthropicApiKey);
  }

  public boolean hasGrokApi() {
    return !isBlank(grokApiKey);
  }
}
