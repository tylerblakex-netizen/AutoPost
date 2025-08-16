package com.autopost;

public final class Env {
    private Env() {}

    public static boolean has(String key) {
        String v = System.getenv(key);
        return v != null && !v.isBlank();
    }
}
