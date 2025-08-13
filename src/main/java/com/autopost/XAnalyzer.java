package com.autopost;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.time.*;
import java.util.*;
import twitter4j.*;

public class XAnalyzer {
  private static final ZoneId LONDON = ZoneId.of("Europe/London");
  private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
  private static final ObjectMapper M = new ObjectMapper();

  public void run() throws Exception {
    var cfg = Config.loadFromEnv();
    var t = TwitterFactory.getSingleton();
    t.setOAuthConsumer(cfg.twApiKey(), cfg.twApiSecret());
    t.setOAuthAccessToken(
        new twitter4j.auth.AccessToken(cfg.twAccessToken(), cfg.twAccessSecret()));
    java.util.List<Status> tweets = new java.util.ArrayList<>();
    long max = -1L;
    for (int page = 0; page < 8; page++) {
      Paging p = new Paging(page + 1, 200);
      if (max > 0) p.setMaxId(max - 1);
      var batch = t.getUserTimeline(t.getScreenName(), p);
      if (batch == null || batch.isEmpty()) break;
      tweets.addAll(batch);
      max = batch.get(batch.size() - 1).getId();
    }
    Map<Integer, Map<Integer, double[]>> b = new HashMap<>();
    for (int d = 0; d < 7; d++) {
      b.put(d, new HashMap<>());
      for (int h = 0; h < 24; h++) b.get(d).put(h, new double[] {0, 0});
    }
    for (Status s : tweets) {
      if (s.isRetweet()) continue;
      var z = s.getCreatedAt().toInstant().atZone(LONDON);
      int d = z.getDayOfWeek().getValue() - 1;
      int h = z.getHour();
      double eng = s.getFavoriteCount() + s.getRetweetCount();
      var cell = b.get(d).get(h);
      cell[0] += 1;
      cell[1] += eng;
    }
    java.util.List<Map<String, Object>> slots = new java.util.ArrayList<>();
    for (int d = 0; d < 7; d++) {
      double best = -1;
      int bestH = 9;
      double a = 1, beta = 1;
      for (int h = 0; h < 24; h++) {
        var c = b.get(d).get(h);
        double score = (c[1] + a) / (c[0] + beta);
        if (score > best) {
          best = score;
          bestH = h;
        }
      }
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("day", DAYS[d]);
      s.put("hour", bestH);
      s.put("score", Math.round(best * 1000) / 1000.0);
      s.put("samples", (int) b.get(d).get(bestH)[0]);
      slots.add(s);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("timezone", "Europe/London");
    out.put("updated_at", Instant.now().toString());
    out.put("slots", slots);
    try (FileOutputStream fos = new FileOutputStream("best_slots.json")) {
      M.writerWithDefaultPrettyPrinter().writeValue(fos, out);
    }
    StringBuilder sb = new StringBuilder("# Best posting times (Europe/London)\n\n");
    for (var s : slots)
      sb.append(s.get("day"))
          .append(" ")
          .append(String.format("%02d:00", (int) s.get("hour")))
          .append("\n");
    try (FileOutputStream fos = new FileOutputStream("analysis.md")) {
      fos.write(sb.toString().getBytes());
    }
  }
}
