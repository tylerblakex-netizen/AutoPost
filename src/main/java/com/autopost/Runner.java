package com.autopost;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class Runner {
  private static final ZoneId LONDON = ZoneId.of("Europe/London");
  private static final ObjectMapper M = new ObjectMapper();

  public void run() throws Exception {
    // Gate by best posting hour if available
    if (!shouldPostNow()) {
      System.out.println("Not in best posting slot now. Skipping.");
      return;
    }

    Config cfg = Config.loadFromEnv();
    DriveService drive = new DriveService(cfg);
    CaptionService captions = new CaptionService(cfg);
    TwitterService twitter = new TwitterService(cfg);
    WebhookPoster webhook = new WebhookPoster(cfg);

    // Find oldest video in RAW
    Map<String, Object> f = drive.listOldestVideo(cfg.rawFolderId());
    if (f == null) {
      System.out.println("No videos found in RAW folder. Nothing to do.");
      return;
    }
    String fileId = String.valueOf(f.get("id"));
    String fileName = String.valueOf(f.get("name"));
    System.out.println("Picked RAW file: " + fileName + " (" + fileId + ")");

    // Infer collaborator
    String collab = Utils.parseCollabFromFilename(fileName);
    String handle = Utils.loadCollabHandle(collab);

    // Download source
    Path tmp = Paths.get(System.getProperty("java.io.tmpdir"));
    Path src = tmp.resolve("source-" + fileId + ".mp4");
    drive.downloadFile(fileId, src);

    // Process
    VideoProcessor vp = new VideoProcessor();
    java.util.List<Path> cuts = vp.makeClips(src); // 3 clips + 1 teaser (last)

    int clipIdx = 0;
    java.util.List<Path> finals = new ArrayList<>();
    Path teaserOut = null;
    for (int i = 0; i < cuts.size(); i++) {
      Path in = cuts.get(i);
      boolean isTeaser = in.getFileName().toString().toLowerCase().contains("teaser");
      String type = isTeaser ? "teaser" : "clip";
      int index = isTeaser ? 1 : (++clipIdx);
      String finalName = FilenameUtil.buildName(collab, type, index);
      Path out1080 = vp.to1080p60(in, finalName);
      finals.add(out1080);
      if (isTeaser) teaserOut = out1080;
    }

    // Generate caption
    CaptionService.Caption cap = captions.generate(fileName, handle != null ? handle : (collab == null ? "none" : collab));
    String text = Utils.joinCaption(cap.caption(), cap.hashtags(), handle);

    // Choose a file to post to X: first clip preferred; otherwise teaser
    Path toPost = finals.stream().filter(p -> p.getFileName().toString().contains("_clip_"))
        .findFirst().orElse(teaserOut);

    String tweetUrl = null;
    if (twitter.hasKeys() && toPost != null) {
      try {
        tweetUrl = twitter.tweetVideo(text, toPost);
        System.out.println("Tweeted: " + tweetUrl);
      } catch (Exception e) {
        System.err.println("Tweet failed: " + e.getMessage());
      }
    } else {
      System.out.println("Twitter keys missing or no file to post; skipping X posting.");
    }

    // Upload outputs to EDITS
    java.util.List<String> uploaded = new ArrayList<>();
    for (Path p : finals) {
      try {
        String id = drive.uploadFile(p, cfg.editsFolderId(), p.getFileName().toString());
        uploaded.add(id);
        System.out.println("Uploaded to EDITS: " + p.getFileName());
      } catch (Exception e) {
        System.err.println("Upload failed for " + p.getFileName() + ": " + e.getMessage());
      }
    }

    // Move RAW into EDITS (archive)
    try {
      drive.moveTo(fileId, cfg.editsFolderId());
      System.out.println("Moved RAW to EDITS.");
    } catch (Exception e) {
      System.err.println("Move RAW failed: " + e.getMessage());
    }

    // Optional webhook (none configured per user, but keep for future)
    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("source", fileName);
      payload.put("uploaded_count", uploaded.size());
      payload.put("tweet", tweetUrl);
      webhook.post(payload);
    } catch (Exception ignore) {}
  }

  private boolean shouldPostNow() {
    String force = System.getenv("FORCE_POST");
    if (force != null && force.equalsIgnoreCase("true")) return true;
    try {
      Path f = Paths.get("best_slots.json");
      if (!Files.exists(f)) return true; // no gating data yet
      JsonNode root = M.readTree(Files.readAllBytes(f));
      JsonNode slots = root.get("slots");
      if (slots == null || !slots.isArray()) return true;
      ZonedDateTime now = ZonedDateTime.now(LONDON);
      String today = now.getDayOfWeek().toString().substring(0,3).substring(0,1).toUpperCase() + now.getDayOfWeek().toString().substring(1,3).toLowerCase();
      int hourNow = now.getHour();
      for (JsonNode s : slots) {
        String day = s.get("day").asText();
        int hour = s.get("hour").asInt();
        if (today.equals(day)) {
          return hourNow == hour;
        }
      }
    } catch (Exception e) {
      System.err.println("Gating read failed: " + e.getMessage());
      return true;
    }
    return true;
  }
}