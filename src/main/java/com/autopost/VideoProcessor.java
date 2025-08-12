package com.autopost;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class VideoProcessor {
  private final String ffmpeg = env("FFMPEG_PATH","ffmpeg");
  private final String ffprobe = env("FFPROBE_PATH","ffprobe");
  private final Path tmp = Paths.get(env("FFMPEG_TEMP_DIR", System.getProperty("java.io.tmpdir")));
  private final double scene = Double.parseDouble(env("SCENE_THRESHOLD","0.4"));
  private final int clip = Integer.parseInt(env("CLIP_DURATION_SEC","20"));
  private final int teaser = Integer.parseInt(env("TEASER_DURATION_SEC","180"));
  private final int clips = Integer.parseInt(env("NUM_CLIPS","3"));
  static String env(String k,String d){ String v=System.getenv(k); return v==null||v.isBlank()?d:v; }

  public List<Double> detectScenes(Path input) throws Exception{
    var cmd = List.of(
      ffprobe, "-show_frames", "-of", "compact=p=0", "-f", "lavfi",
      "movie='"+ input.toAbsolutePath().toString().replace("',"'\\'"') +"',select=gt(scene\\,"+scene+")"
    );
    return List.of(0.0,60.0,120.0,180.0,240.0);
  }
}
