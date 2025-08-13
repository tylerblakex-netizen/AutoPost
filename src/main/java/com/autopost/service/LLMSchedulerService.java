package com.autopost.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LLMSchedulerService {

  @Value("${openai.api.key}")
  private String openAiApiKey;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Path statePath = Paths.get("./state");
  private final Path nextRunPath = statePath.resolve("next_run.json");
  private final Path postedPath = statePath.resolve("posted");
  private final Path historyPath = statePath.resolve("post_history.json");

  // Run daily at 00:05 London time to plan today's post
  @Scheduled(cron = "0 5 0 * * *", zone = "Europe/London")
  public void planDailyPost() throws IOException {
    System.out.println("Planning today's post time with LLM...");

    // Check if OpenAI API key is available
    if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
      System.out.println("OpenAI API key not configured, using fallback scheduling.");
      scheduleWithFallback();
      return;
    }

    // Check if we already have a time for today
    if (hasPlannedTimeForToday()) {
      System.out.println("Already have a planned time for today, skipping.");
      return;
    }

    // Get posting history
    List<PostHistory> history = loadPostHistory();

    // Get recent used minutes (last 14 days) to avoid
    Set<String> recentMinutes = getRecentMinutes(history, 14);

    // Ask OpenAI for optimal time
    PostTime optimalTime = getOptimalTimeFromLLM(history, recentMinutes);

    // Save the scheduled time
    saveNextRun(optimalTime);

    // Schedule the actual post
    schedulePost(optimalTime.timestamp);
  }

  private void scheduleWithFallback() throws IOException {
    Set<String> recentMinutes = new HashSet<>(); // No history to avoid
    PostTime fallbackTime = generateFallbackTime(recentMinutes);
    saveNextRun(fallbackTime);
    schedulePost(fallbackTime.timestamp);
  }

  private boolean hasPlannedTimeForToday() throws IOException {
    if (!Files.exists(nextRunPath)) {
      return false;
    }

    String content = Files.readString(nextRunPath);
    ObjectNode nextRun = (ObjectNode) objectMapper.readTree(content);

    if (!nextRun.has("timestamp")) {
      return false;
    }

    ZonedDateTime scheduled = ZonedDateTime.parse(nextRun.get("timestamp").asText());
    LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));

    return scheduled.toLocalDate().equals(today);
  }

  private PostTime getOptimalTimeFromLLM(List<PostHistory> history, Set<String> avoidMinutes)
      throws IOException {
    String prompt = buildPrompt(history, avoidMinutes);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(openAiApiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-4");
    requestBody.put(
        "messages",
        Arrays.asList(
            Map.of(
                "role",
                "system",
                "content",
                "You are a social media optimization expert. Analyze posting history and suggest optimal posting times."),
            Map.of("role", "user", "content", prompt)));
    requestBody.put("temperature", 0.7);
    requestBody.put("max_tokens", 200);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(
              "https://api.openai.com/v1/chat/completions", HttpMethod.POST, request, String.class);

      return parseOpenAIResponse(response.getBody(), avoidMinutes);
    } catch (Exception e) {
      System.err.println("Error calling OpenAI: " + e.getMessage());
      // Fallback to random time if OpenAI fails
      return generateFallbackTime(avoidMinutes);
    }
  }

  private String buildPrompt(List<PostHistory> history, Set<String> avoidMinutes) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "Based on the following posting history, suggest the optimal time to post today in London timezone.\n\n");

    if (!history.isEmpty()) {
      prompt.append("Recent posts performance:\n");
      for (PostHistory post : history.stream().limit(30).collect(Collectors.toList())) {
        prompt.append(
            String.format(
                "- %s (%s): %d impressions, %d likes, %.2f watch ratio\n",
                post.datetime, post.weekday, post.impressions, post.likes, post.watchTimeRatio));
      }
      prompt.append("\n");
    }

    prompt.append("Requirements:\n");
    prompt.append("- Time must be between 07:00 and 23:30 London time today\n");
    prompt.append("- Avoid round times (:00, :30)\n");
    prompt
        .append("- These exact minutes were used recently, do NOT use: ")
        .append(avoidMinutes)
        .append("\n");
    prompt.append(
        "\nReturn ONLY a JSON object with: {\"time\": \"HH:MM\", \"reason\": \"brief explanation\", \"confidence\": 0.0-1.0}");

    return prompt.toString();
  }

  private PostTime parseOpenAIResponse(String response, Set<String> avoidMinutes)
      throws IOException {
    ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response);
    String content = responseJson.path("choices").get(0).path("message").path("content").asText();

    // Parse the JSON from content
    ObjectNode suggestion = (ObjectNode) objectMapper.readTree(content);

    String timeStr = suggestion.get("time").asText();
    String reason = suggestion.get("reason").asText();
    double confidence = suggestion.get("confidence").asDouble();

    // Validate and adjust if needed
    LocalTime time = LocalTime.parse(timeStr);

    // If the suggested minute is in avoid list or is round, adjust it
    if (avoidMinutes.contains(timeStr.substring(3))
        || time.getMinute() == 0
        || time.getMinute() == 30) {
      time = adjustTime(time, avoidMinutes);
    }

    ZonedDateTime timestamp =
        ZonedDateTime.of(
            LocalDate.now(ZoneId.of("Europe/London")), time, ZoneId.of("Europe/London"));

    return new PostTime(timestamp, reason, confidence);
  }

  private LocalTime adjustTime(LocalTime time, Set<String> avoidMinutes) {
    // Add/subtract a few minutes to avoid conflicts
    for (int offset = 1; offset <= 29; offset++) {
      for (int sign : new int[] {1, -1}) {
        LocalTime adjusted = time.plusMinutes(offset * sign);
        String minute = String.format("%02d", adjusted.getMinute());

        if (!avoidMinutes.contains(minute)
            && adjusted.getMinute() != 0
            && adjusted.getMinute() != 30) {
          if (adjusted.isAfter(LocalTime.of(7, 0)) && adjusted.isBefore(LocalTime.of(23, 30))) {
            return adjusted;
          }
        }
      }
    }
    return time;
  }

  private PostTime generateFallbackTime(Set<String> avoidMinutes) {
    LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));

    // Generate random time between 07:00 and 23:30
    int hour = ThreadLocalRandom.current().nextInt(7, 24);
    int minute;

    do {
      minute = ThreadLocalRandom.current().nextInt(1, 60);
    } while (minute == 0 || minute == 30 || avoidMinutes.contains(String.format("%02d", minute)));

    if (hour == 23 && minute > 30) {
      minute = ThreadLocalRandom.current().nextInt(1, 30);
    }

    ZonedDateTime timestamp =
        ZonedDateTime.of(today, LocalTime.of(hour, minute), ZoneId.of("Europe/London"));

    return new PostTime(timestamp, "Fallback random time (OpenAI unavailable)", 0.5);
  }

  private List<PostHistory> loadPostHistory() throws IOException {
    if (!Files.exists(historyPath)) {
      return new ArrayList<>();
    }

    String content = Files.readString(historyPath);
    return objectMapper.readValue(
        content,
        objectMapper.getTypeFactory().constructCollectionType(List.class, PostHistory.class));
  }

  private Set<String> getRecentMinutes(List<PostHistory> history, int days) {
    ZonedDateTime cutoff = ZonedDateTime.now(ZoneId.of("Europe/London")).minusDays(days);

    return history.stream()
        .filter(h -> ZonedDateTime.parse(h.datetime).isAfter(cutoff))
        .map(
            h -> {
              ZonedDateTime dt = ZonedDateTime.parse(h.datetime);
              return String.format("%02d", dt.getMinute());
            })
        .collect(Collectors.toSet());
  }

  private void saveNextRun(PostTime postTime) throws IOException {
    Files.createDirectories(statePath);

    ObjectNode nextRun = objectMapper.createObjectNode();
    nextRun.put("timestamp", postTime.timestamp.toString());
    nextRun.put("reason", postTime.reason);
    nextRun.put("confidence", postTime.confidence);
    nextRun.put("strategy", "llm");
    nextRun.put("planned_at", ZonedDateTime.now(ZoneId.of("Europe/London")).toString());

    Files.writeString(
        nextRunPath, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(nextRun));
    System.out.println("Saved next run time: " + postTime.timestamp);
  }

  private void schedulePost(ZonedDateTime timestamp) {
    // This will be handled by the PostingService
    System.out.println("Post scheduled for: " + timestamp);
  }

  static class PostTime {
    final ZonedDateTime timestamp;
    final String reason;
    final double confidence;

    PostTime(ZonedDateTime timestamp, String reason, double confidence) {
      this.timestamp = timestamp;
      this.reason = reason;
      this.confidence = confidence;
    }
  }

  static class PostHistory {
    public String datetime;
    public String weekday;
    public int impressions;
    public int likes;
    public int comments;
    public int reposts;
    public double watchTimeRatio;
    public int videoLengthSec;
  }
}
