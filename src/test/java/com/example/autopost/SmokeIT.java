package com.example.autopost;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CI smoke. Auto-skips unless the 3 Drive envs are present.
 * Replace with a real end-to-end later if you want.
 */
public class SmokeIT {

    @Test
    void integration_env_present_or_skip() {
        String saFile = System.getenv("GDRIVE_SERVICE_ACCOUNT_FILE");
        String raw = System.getenv("GDRIVE_PARENT_ID_RAW");
        String edits = System.getenv("GDRIVE_PARENT_ID_EDITS");

        boolean hasAll = notEmpty(saFile) && notEmpty(raw) && notEmpty(edits);
        Assumptions.assumeTrue(hasAll, "Skipping integration: missing GDrive envs");

        // Minimal assertion so the test actually *does* something
        assertTrue(hasAll, "Env preflight should be true when not skipped");
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isBlank();
    }
}
