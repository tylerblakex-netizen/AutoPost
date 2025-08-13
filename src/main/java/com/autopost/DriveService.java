package com.autopost;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DriveService {
  private final Drive drive;

  public DriveService(Config cfg) throws Exception {
    GoogleCredentials creds;
    if (cfg.hasInlineSA())
      creds =
          ServiceAccountCredentials.fromStream(
                  new ByteArrayInputStream(cfg.saInlineJson().getBytes()))
              .createScoped(Set.of(DriveScopes.DRIVE));
    else if (cfg.hasSAPath())
      creds =
          ServiceAccountCredentials.fromStream(new FileInputStream(cfg.saPath()))
              .createScoped(Set.of(DriveScopes.DRIVE));
    else throw new RuntimeException("Service account credentials not provided");
    drive =
        new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(creds))
            .setApplicationName("AutoPost")
            .build();
  }

  public Map<String, Object> listOldestVideo(String folderId) throws IOException {
    var q = "'" + folderId + "' in parents and mimeType contains 'video/' and trashed=false";
    FileList list =
        drive
            .files()
            .list()
            .setQ(q)
            .setFields("files(id,name,createdTime,parents,webViewLink)")
            .setOrderBy("createdTime")
            .setPageSize(10)
            .execute();
    var files = list.getFiles();
    if (files == null || files.isEmpty()) return null;
    File f = files.get(0);
    return new java.util.LinkedHashMap<>() {
      {
        put("id", f.getId());
        put("name", f.getName());
      }
    };
  }

  public String ensureAnyoneView(String fileId) throws IOException {
    try {
      var p = new com.google.api.services.drive.model.Permission();
      p.setType("anyone");
      p.setRole("reader");
      drive.permissions().create(fileId, p).execute();
    } catch (IOException ignored) {
    }
    return drive.files().get(fileId).setFields("id,webViewLink").execute().getWebViewLink();
  }

  public void downloadFile(String fileId, Path dest) throws IOException {
    try (OutputStream os = Files.newOutputStream(dest)) {
      drive.files().get(fileId).executeMediaAndDownloadTo(os);
    }
  }

  public void moveTo(String fileId, String newParentId) throws IOException {
    var f = drive.files().get(fileId).setFields("parents").execute();
    var prev = String.join(",", f.getParents() == null ? java.util.List.of() : f.getParents());
    drive
        .files()
        .update(fileId, null)
        .setAddParents(newParentId)
        .setRemoveParents(prev)
        .setFields("id,parents")
        .execute();
  }

  public String uploadFile(Path file, String folderId, String name) throws IOException {
    var meta = new com.google.api.services.drive.model.File();
    meta.setName(name);
    meta.setParents(java.util.List.of(folderId));
    var media = new com.google.api.client.http.FileContent("video/mp4", file.toFile());
    return drive.files().create(meta, media).setFields("id").execute().getId();
  }
}
