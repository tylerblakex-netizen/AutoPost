# Testing Approach

## Safer Testing Without Reflection

This project uses a safer testing approach that avoids reflection-based environment manipulation.

### What Was Changed

Previously, tests used reflection to modify the system environment:

```java
// OLD APPROACH - Fragile and JVM-dependent
private static void setEnv(String key, String value) throws Exception {
  Map<String, String> env = System.getenv();
  Field field = env.getClass().getDeclaredField("m");
  field.setAccessible(true);
  @SuppressWarnings("unchecked")
  Map<String, String> writableEnv = (Map<String, String>) field.get(env);
  writableEnv.put(key, value);
}
```

### New Approach

The new approach creates `Config` objects directly with test data:

```java
// NEW APPROACH - Safe and portable
Config cfg = new Config(
    "test-openai-key",
    "gpt-4",
    "raw-folder-id",
    "edits-folder-id", 
    "https://webhook.example.com",
    "/path/to/sa.json",
    "{\"type\":\"service_account\"}",
    "twitter-api-key",
    "twitter-api-secret",
    "twitter-access-token",
    "twitter-access-secret"
);
```

### Benefits

1. **Portable**: Works across all JVM implementations
2. **Reliable**: No reliance on JVM internals
3. **Clear**: Tests focus on business logic rather than environment manipulation
4. **Maintainable**: No complex reflection code to maintain
5. **Fast**: No overhead from environment manipulation

### Running Tests

```bash
mvn test
```

All tests pass without requiring environment variable manipulation or reflection.