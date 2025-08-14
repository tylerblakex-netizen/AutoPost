package com.autopost;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ConfigTest {
  @Test
  void loadsServiceCredentialsFromEnv() throws Exception {
    setEnv("OPENAI_API_KEY", "test");
    setEnv("RAW_FOLDER_ID", "raw");
    setEnv("EDITS_FOLDER_ID", "edits");
    setEnv("SERVICE_PUBLIC_ID", "public");
    setEnv("SERVICE_SECRET_KEY", "secret");
    Config cfg = Config.loadFromEnv();
    assertEquals("public", cfg.servicePublicId());
    assertEquals("secret", cfg.serviceSecretKey());
  }

  private static void setEnv(String key, String value) throws Exception {
    Map<String, String> env = System.getenv();
    Field field = env.getClass().getDeclaredField("m");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, String> writableEnv = (Map<String, String>) field.get(env);
    writableEnv.put(key, value);
  }
}
