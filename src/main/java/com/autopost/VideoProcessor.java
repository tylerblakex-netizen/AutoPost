package com.autopost;
import java.io.*; import java.nio.file.*; import java.util.*; import java.util.stream.Collectors;
public class VideoProcessor {
  private final String ffmpeg=env("FFMPEG_PATH","ffmpeg"), ffprobe=env("FFPROBE_PATH","ffprobe");
  private final Path tmp=Paths.get(env("FFMPEG_TEMP_DIR", System.getProperty("java.io.tmpdir")));
  private final double scene=Double.parseDouble(env("SCENE_THRESHOLD","0.4"));
  private final int clip= Integer.parseInt(env("CLIP_DURATION_SEC","20"));
  private final int teaser= Integer.parseInt(env("TEASER_DURATION_SEC","180"));
  private final int clips = Integer.parseInt(env("NUM_CLIPS","3"));
  static String env(String k,String d){ var v=System.getenv(k); return v==null||v.isBlank()?d:v; }

  public List<Double> detectScenes(Path input) throws Exception{
    var cmd=List.of(ffprobe,"-show_frames","-of","compact=p=0","-f","lavfi","movie='"+input.toAbsolutePath().toString().replace(\"'\",\"'\\\\''\")+"',select=gt(scene\\,"+scene+")");
    var p=new ProcessBuilder(cmd).redirectErrorStream(true).start(); List<String> out;
    try(var br=new BufferedReader(new InputStreamReader(p.getInputStream()))){ out=br.lines().collect(Collectors.toList()); } p.waitFor();
    List<Double> pts=new ArrayList<>(); for(String line: out){ int i=line.indexOf("pkt_pts_time="); if(i>=0){ int j=line.indexOf('|',i); String v=(j>i? line.substring(i+13,j): line.substring(i+13)); try{ pts.add(Double.parseDouble(v)); }catch(Exception ignore){} } }
    if(pts.isEmpty()) pts=List.of(0.0,60.0,120.0,180.0,240.0); return pts;
  }
  public Path cut(Path in,double start,double dur,String name) throws Exception{
    Path out=tmp.resolve(name); var cmd=List.of(ffmpeg,"-ss",String.valueOf(start),"-i",in.toString(),"-t",String.valueOf(dur),
      "-c:v","libx264","-preset","fast","-crf","23","-c:a","aac","-b:a","192k",out.toString()); run(cmd); return out;
  }
  public Path to1080p60(Path in,String name) throws Exception{
    Path out=tmp.resolve(name); var cmd=List.of(ffmpeg,"-i",in.toString(),"-vf","scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2",
      "-r","60","-c:v","libx264","-preset","fast","-crf","23","-c:a","aac","-b:a","256k",out.toString()); run(cmd); return out;
  }
  public java.util.List<Path> makeClips(Path in) throws Exception{
    var t=detectScenes(in); var outs=new java.util.ArrayList<Path>(); int n=Math.min(clips, Math.max(0,t.size()-1));
    for(int i=0;i<n;i++){ double s=t.get(i); double d=Math.min(clip,(i+1<t.size()? t.get(i+1)-s: clip)); outs.add(cut(in,s,d,"clip_"+(i+1)+".mp4")); }
    outs.add(cut(in,t.get(0),teaser,"teaser.mp4")); return outs;
  }
  private void run(java.util.List<String> cmd) throws Exception{ var p=new ProcessBuilder(cmd).redirectErrorStream(true).start(); try(var br=new BufferedReader(new InputStreamReader(p.getInputStream()))){ while(br.readLine()!=null){} } int c=p.waitFor(); if(c!=0) throw new RuntimeException("ffmpeg/ffprobe exited "+c); }
}
