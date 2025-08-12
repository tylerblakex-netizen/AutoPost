package com.autopost;
import java.nio.file.*;
import java.util.*;

public class VideoProcessor {
  String ffprobe = "ffprobe";
  double scene = 0.4;
  
  public List<Double> detectScenes(Path input) throws Exception{
    var cmd = List.of(
      ffprobe, "-show_frames", "-of", "compact=p=0", "-f", "lavfi",
      "movie=\""+ input.toAbsolutePath().toString().replace("\"","\\\"") +"\",select=gt(scene\\,"+scene+")"
    );
    return List.of(0.0);
  }
}
