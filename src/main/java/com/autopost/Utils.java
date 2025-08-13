package com.autopost;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

public class Utils {
  private static final ObjectMapper M = new ObjectMapper();
  private static final Set<String> GENERIC = Set.of("teaser", "clip", "video");

  public static String parseCollabFromFilename(String name) {
    String base = name.replaceAll("\\.[^.]*$", "");
    String[] parts = base.split("[-_]", 2);
    String token = parts[0].trim();
    return token.isBlank() || GENERIC.contains(token.toLowerCase()) ? null : token;
  }

  public static String loadCollabHandle(String collab) {
    if (collab == null) return null;
    try (InputStream is = Utils.class.getResourceAsStream("/collabs.json")) {
      Map<?, ?> map = M.readValue(is, Map.class);
      Object v = map.get(collab);
      return v == null ? null : v.toString();
    } catch (Exception e) {
      return null;
    }
  }

  public static String joinCaption(String caption, java.util.List<String> tags, String handle) {
    String text = caption == null ? "" : caption.trim();
    if (handle != null && !handle.isBlank()) text = (text + " " + handle).trim();
    if (tags != null && !tags.isEmpty()) {
      StringBuilder sb = new StringBuilder(text).append("\n");
      int n = 0;
      for (String h : tags) {
        if (n++ >= 3) break;
        if (h == null || h.isBlank()) continue;
        sb.append('#').append(h.replace("#", "").trim()).append(' ');
      }
      text = sb.toString().trim();
    }
    return text;
  }
}
