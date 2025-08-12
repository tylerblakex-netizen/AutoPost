package com.autopost.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
public class PostingService {
    
    @Value("${google.service.account.json}")
    private String serviceAccountPath;
    
    @Value("${raw.folder.id}")
    private String rawFolderId;
    
    @Value("${edits.folder.id}")
    private String editsFolderId;
    
    @Value("${twitter.api.key}")
    private String twitterApiKey;
    
    @Value("${twitter.api.secret}")
    private String twitterApiSecret;
    
    @Value("${twitter.access.token}")
    private String twitterAccessToken;
    
    @Value("${twitter.access.secret}")
    private String twitterAccessSecret;
    
    @Value("${openai.api.key}")
    private String openAiApiKey;
    
    private final TaskScheduler taskScheduler;
    private final Path statePath = Paths.get("./state");
    private final Path nextRunPath = statePath.resolve("next_run.json");
    private final Path postedPath = statePath.resolve("posted");
    
    private Twitter twitter;
    private Drive driveService;
    private ScheduledFuture<?> currentScheduledPost;
    
    public PostingService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }
    
    @PostConstruct
    public void init() throws Exception {
        // Initialize Twitter
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(twitterApiKey)
            .setOAuthConsumerSecret(twitterApiSecret)
            .setOAuthAccessToken(twitterAccessToken)
            .setOAuthAccessTokenSecret(twitterAccessSecret);
        
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        
        // Initialize Google Drive
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath))
            .createScoped(Arrays.asList("https://www.googleapis.com/auth/drive.readonly"));
        
        driveService = new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
            .setApplicationName("AutoPost")
            .build();
        
        // Check for existing scheduled post
        loadScheduledPost();
    }
    
    // Check every minute if it's time to post
    @Scheduled(fixedDelay = 60000)
    public void checkAndPost() throws Exception {
        if (!Files.exists(nextRunPath)) {
            return;
        }
        
        String content = Files.readString(nextRunPath);
        Map<String, Object> nextRun = new com.fasterxml.jackson.databind.ObjectMapper().readValue(content, Map.class);
        
        ZonedDateTime scheduledTime = ZonedDateTime.parse((String) nextRun.get("timestamp"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/London"));
        
        // Check if it's time to post (within 1 minute window)
        if (now.isAfter(scheduledTime) && now.isBefore(scheduledTime.plusMinutes(1))) {
            executePost();
        }
    }
    
    private void executePost() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));
        Path todayMarker = postedPath.resolve(today.format(DateTimeFormatter.ISO_DATE) + ".teaser");
        
        // Check if already posted today
        if (Files.exists(todayMarker)) {
            System.out.println("Already posted today, skipping.");
            return;
        }
        
        // Check for RAW video
        File rawVideo = downloadLatestRawVideo();
        if (rawVideo == null) {
            System.out.println("No RAW video available, skipping post.");
            return;
        }
        
        try {
            // Process video
            File[] clips = createClips(rawVideo);
            File teaser = createTeaser(clips);
            
            // Generate caption
            String caption = generateCaption(teaser);
            
            // Upload to Twitter
            uploadAndPost(teaser, caption);
            
            // Mark as posted
            Files.createDirectories(postedPath);
            Files.createFile(todayMarker);
            
            // Clear next_run.json for tomorrow's planning
            Files.deleteIfExists(nextRunPath);
            
            // Log success
            logPostSuccess(today, caption);
            
        } finally {
            // Cleanup temporary files
            rawVideo.delete();
        }
    }
    
    private File downloadLatestRawVideo() throws IOException {
        try {
            // List files in RAW folder
            Drive.Files.List request = driveService.files().list()
                .setQ("'" + rawFolderId + "' in parents and mimeType='video/mp4' and trashed=false")
                .setOrderBy("createdTime desc")
                .setPageSize(1);
            
            com.google.api.services.drive.model.FileList files = request.execute();
            
            if (files.getFiles().isEmpty()) {
                return null;
            }
            
            // Download the latest file
            String fileId = files.getFiles().get(0).getId();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            
            File tempFile = File.createTempFile("raw_", ".mp4");
            Files.write(tempFile.toPath(), outputStream.toByteArray());
            
            return tempFile;
            
        } catch (Exception e) {
            System.err.println("Error downloading from Drive: " + e.getMessage());
            return null;
        }
    }
    
    private File[] createClips(File rawVideo) throws IOException {
        // Use ffmpeg to create 3x20s clips
        File[] clips = new File[3];
        
        for (int i = 0; i < 3; i++) {
            clips[i] = File.createTempFile("clip_" + i + "_", ".mp4");
            int startTime = i * 60; // Start at 0s, 60s, 120s
            
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", rawVideo.getAbsolutePath(),
                "-ss", String.valueOf(startTime),
                "-t", "20",
                "-c", "copy",
                clips[i].getAbsolutePath()
            );
            
            Process process = pb.start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return clips;
    }
    
    private File createTeaser(File[] clips) throws IOException {
        // Concatenate clips into 180s teaser
        File teaser = File.createTempFile("teaser_", ".mp4");
        
        // Create concat file
        File concatFile = File.createTempFile("concat_", ".txt");
        StringBuilder concat = new StringBuilder();
        for (File clip : clips) {
            concat.append("file '").append(clip.getAbsolutePath()).append("'\n");
        }
        Files.writeString(concatFile.toPath(), concat.toString());
        
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-f", "concat", "-safe", "0",
            "-i", concatFile.getAbsolutePath(),
            "-c", "copy",
            "-t", "180",
            teaser.getAbsolutePath()
        );
        
        Process process = pb.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Cleanup
        concatFile.delete();
        for (File clip : clips) {
            clip.delete();
        }
        
        return teaser;
    }
    
    private String generateCaption(File video) {
        // Use OpenAI to generate caption
        try {
            // This would analyze the video and generate appropriate caption
            // For now, return a placeholder
            return "Check out today's highlights! 🎬 #content #video #daily";
        } catch (Exception e) {
            return "Today's video is ready! #content";
        }
    }
    
    private void uploadAndPost(File video, String caption) throws TwitterException, IOException {
        // Upload video to Twitter
        long[] mediaIds = new long[1];
        UploadedMedia media = twitter.uploadMedia(video);
        mediaIds[0] = media.getMediaId();
        
        // Post tweet with video
        StatusUpdate status = new StatusUpdate(caption);
        status.setMediaIds(mediaIds);
        
        twitter.updateStatus(status);
        System.out.println("Successfully posted to Twitter!");
    }
    
    private void loadScheduledPost() throws IOException {
        if (Files.exists(nextRunPath)) {
            String content = Files.readString(nextRunPath);
            System.out.println("Loaded scheduled post: " + content);
        }
    }
    
    private void logPostSuccess(LocalDate date, String caption) throws IOException {
        Path logPath = statePath.resolve("post_log.txt");
        Files.createDirectories(statePath);
        
        String logEntry = String.format("%s - Posted successfully. Caption: %s%n", 
            date.format(DateTimeFormatter.ISO_DATE), caption);
        
        Files.write(logPath, logEntry.getBytes(), 
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}