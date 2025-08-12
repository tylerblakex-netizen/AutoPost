package com.autopost;
import java.nio.file.*; import java.time.*; import java.util.*; import java.util.logging.Logger;
public class Runner {
  private static final Logger log=Logger.getLogger("AutoPost"); private static final ZoneId LONDON=ZoneId.of("Europe/London"); private static final Map<String,Integer> DOW=Map.of("Mon",0,"Tue",1,"Wed",2,"Thu",3,"Fri",4,"Sat",5,"Sun",6);
  private boolean inBestSlot(){ Path p=Paths.get("best_slots.json"); if(!Files.exists(p)) return ZonedDateTime.now(LONDON).getHour()==9;
    try{ var root=new com.fasterxml.jackson.databind.ObjectMapper().readTree(Files.readString(p)); var now=ZonedDateTime.now(LONDON); int d=now.getDayOfWeek().getValue()-1; int h=now.getHour(); for(var s: root.get("slots")) if(DOW.get(s.get("day").asText())==d && s.get("hour").asInt()==h) return true; return false; }catch(Exception e){ return true; } }
  public void run() throws Exception {
    if(!inBestSlot()){ log.info("Not in best posting slot now. Skipping."); return; }
    var cfg=Config.loadFromEnv(); var drive=new DriveService(cfg); var webhook=new WebhookPoster(cfg); var captions=new CaptionService(cfg); var twitter=new TwitterService(cfg); var video=new VideoProcessor();
    var f=drive.listOldestVideo(cfg.rawFolderId()); if(f==null){ log.info("No files in RAW. Exiting."); return; } String fileId=(String)f.get("id"), fileName=(String)f.get("name");
    String collab=Utils.parseCollabFromFilename(fileName); String handle=Utils.loadCollabHandle(collab);
    Path src=Files.createTempFile("autopost_src_",".mp4"); drive.downloadFile(fileId,src);
    var outs=video.makeClips(src); Path first=outs.get(0); Path clip1080=video.to1080p60(first,"clip_post.mp4");
    int i=1; for(Path pth: outs){ String type=pth.getFileName().toString().contains("teaser")? "teaser":"clip"; String safe=FilenameUtil.buildName(collab,type,i++); drive.uploadFile(pth,cfg.editsFolderId(),safe); try{ Files.deleteIfExists(pth);}catch(Exception ignore){} }
    var cap=captions.generate(fileName, handle!=null? handle: collab); String text=Utils.joinCaption(cap.caption(), cap.hashtags(), handle); String web=drive.ensureAnyoneView(fileId);
    boolean posted=false; if(twitter.hasKeys()) try{ String url=twitter.tweetVideo(text,clip1080); posted=true; log.info("Tweet posted: "+url);}catch(Exception e){ log.warning("X posting failed: "+e.getMessage()); }
    if(!posted && cfg.webhookUrl()!=null && !cfg.webhookUrl().isBlank()){ var payload=new java.util.LinkedHashMap<String,Object>(); payload.put("title",fileName); payload.put("drive_file_id",fileId); payload.put("drive_web_link",web); payload.put("caption",text); payload.put("hashtags",cap.hashtags()); payload.put("picked_at",java.time.Instant.now().toString()); webhook.post(payload); }
    drive.moveTo(fileId, cfg.editsFolderId()); try{ Files.deleteIfExists(src);}catch(Exception ignore){} try{ Files.deleteIfExists(clip1080);}catch(Exception ignore){} log.info("Done.");
  }
}
