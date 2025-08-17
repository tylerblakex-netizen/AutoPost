package com.example.autopost;

import org.junit.jupiter.api.Test;

public class PrintCurrentNames {
  @Test void printOnce() {
    String[] inputs = {
      "input.mp4",
      "very_long_title_with_over_255_chars_" + "a".repeat(250) + ".mp4",
      "unicode_😊.mp4",
      "punct_!@#.mp4",
      "clip_001.mp4",
      "input.mp4"
    };
    int[] idx = {1,1,1,1,1,1};
    boolean[] teaser = {false,false,false,false,false,true};
    for (int i = 0; i < inputs.length; i++) {
      try {
        String out = AutoPostUtils.getClipFilename(inputs[i], idx[i], teaser[i]);
        System.out.println("NAMING ▶ " + inputs[i] + " idx=" + idx[i] + " teaser=" + teaser[i] + " → " + out);
      } catch (Throwable t) {
        System.out.println("NAMING ▶ ERROR: " + t.getMessage());
      }
    }
  }
}
