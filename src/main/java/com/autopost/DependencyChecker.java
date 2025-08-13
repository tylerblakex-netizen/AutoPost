package com.autopost;

import java.util.logging.Logger;

public class DependencyChecker {
    private static final Logger log = Logger.getLogger(DependencyChecker.class.getName());
    
    public static boolean checkFFmpeg() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.warning("FFmpeg not found: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean checkFFprobe() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.warning("FFprobe not found: " + e.getMessage());
            return false;
        }
    }
    
    public static void logSystemInfo() {
        log.info("System Information:");
        log.info("Java Version: " + System.getProperty("java.version"));
        log.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        log.info("Working Directory: " + System.getProperty("user.dir"));
        log.info("Temp Directory: " + System.getProperty("java.io.tmpdir"));
        
        log.info("Dependency Check:");
        log.info("FFmpeg available: " + checkFFmpeg());
        log.info("FFprobe available: " + checkFFprobe());
    }
}