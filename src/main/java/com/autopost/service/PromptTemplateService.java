package com.autopost.service;

import com.autopost.config.ConfigProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Service for managing LLM prompt templates with parameter substitution and safety rails.
 * Templates are versioned and stored in src/main/resources/prompts/.
 */
@Service
public class PromptTemplateService {

  private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);

  private final ConfigProperties config;

  public PromptTemplateService(ConfigProperties config) {
    this.config = config;
  }

  /**
   * Loads and processes the caption generation template with parameters.
   *
   * @param videoContext description of the video content
   * @param additionalContext any additional context for caption generation
   * @param tone the desired tone for the caption (default: engaging)
   * @return the processed prompt ready for LLM
   */
  public String getCaptionPrompt(String videoContext, String additionalContext, String tone) {
    try {
      String template = loadTemplate("caption_template.md");
      
      // Apply parameter substitutions
      String prompt = template
          .replace("{maxHashtags}", String.valueOf(config.getMaxHashtags()))
          .replace("{tone}", tone != null ? tone : "engaging and conversational")
          .replace("{videoContext}", videoContext != null ? videoContext : "Video content analysis")
          .replace("{additionalContext}", additionalContext != null ? additionalContext : "");

      log.debug("Generated caption prompt with {} max hashtags", config.getMaxHashtags());
      return prompt;
    } catch (IOException e) {
      log.error("Failed to load caption template: {}", e.getMessage());
      return getFallbackCaptionPrompt(videoContext, additionalContext, tone);
    }
  }

  /**
   * Loads and processes the scheduling optimization template with parameters.
   *
   * @param historyData historical posting performance data
   * @param timeWindow allowed posting time window
   * @param avoidTimes recently used times to avoid
   * @param dayType weekday or weekend
   * @param contentType type of content being posted
   * @return the processed prompt ready for LLM
   */
  public String getSchedulingPrompt(
      String historyData, String timeWindow, String avoidTimes, String dayType, String contentType) {
    try {
      String template = loadTemplate("scheduling_template.md");
      
      // Apply parameter substitutions
      String prompt = template
          .replace("{timeWindow}", timeWindow != null ? timeWindow : "08:00-22:00 Europe/London")
          .replace("{avoidTimes}", avoidTimes != null ? avoidTimes : "none")
          .replace("{dayType}", dayType != null ? dayType : "weekday")
          .replace("{contentType}", contentType != null ? contentType : "video teaser")
          .replace("{historyData}", historyData != null ? historyData : "No historical data available");

      log.debug("Generated scheduling prompt for {} with time window {}", dayType, timeWindow);
      return prompt;
    } catch (IOException e) {
      log.error("Failed to load scheduling template: {}", e.getMessage());
      return getFallbackSchedulingPrompt(historyData, timeWindow, avoidTimes, dayType, contentType);
    }
  }

  /**
   * Loads a template file from the classpath.
   *
   * @param templateName the name of the template file
   * @return the template content as a string
   * @throws IOException if the template cannot be loaded
   */
  private String loadTemplate(String templateName) throws IOException {
    ClassPathResource resource = new ClassPathResource("prompts/" + templateName);
    
    if (!resource.exists()) {
      throw new IOException("Template not found: " + templateName);
    }

    try (InputStream inputStream = resource.getInputStream()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Fallback caption prompt when template loading fails.
   */
  private String getFallbackCaptionPrompt(String videoContext, String additionalContext, String tone) {
    log.warn("Using fallback caption prompt");
    return String.format(
        "Generate an engaging, algorithm-safe social media caption for this video content: %s. "
        + "Keep it under 280 characters and include exactly %d relevant hashtags. "
        + "Use a %s tone and avoid explicit content. Additional context: %s",
        videoContext != null ? videoContext : "Video content",
        config.getMaxHashtags(),
        tone != null ? tone : "engaging",
        additionalContext != null ? additionalContext : "");
  }

  /**
   * Fallback scheduling prompt when template loading fails.
   */
  private String getFallbackSchedulingPrompt(
      String historyData, String timeWindow, String avoidTimes, String dayType, String contentType) {
    log.warn("Using fallback scheduling prompt");
    return String.format(
        "Based on this posting history data: %s, recommend the optimal posting time for tomorrow "
        + "within the window %s. Avoid these recent times: %s. Consider this is a %s and the content is %s. "
        + "Respond with a JSON object containing timestamp (UTC), confidence, and reason.",
        historyData != null ? historyData : "No data",
        timeWindow != null ? timeWindow : "08:00-22:00 Europe/London",
        avoidTimes != null ? avoidTimes : "none",
        dayType != null ? dayType : "weekday",
        contentType != null ? contentType : "video teaser");
  }

  /**
   * Gets the template ID for logging purposes. Only the template ID should be logged, never the
   * actual prompt content.
   *
   * @param templateName the template name
   * @return a loggable template identifier
   */
  public String getTemplateId(String templateName) {
    return templateName.replace(".md", "") + "_v1.0";
  }

  /**
   * Validates that a prompt follows safety guidelines.
   *
   * @param prompt the prompt to validate
   * @return true if the prompt is safe to use
   */
  public boolean isPromptSafe(String prompt) {
    if (prompt == null || prompt.trim().isEmpty()) {
      return false;
    }

    // Check for safety rails
    String lowerPrompt = prompt.toLowerCase();
    return lowerPrompt.contains("algorithm-safe")
        && lowerPrompt.contains("non-explicit")
        && prompt.contains("" + config.getMaxHashtags());
  }
}