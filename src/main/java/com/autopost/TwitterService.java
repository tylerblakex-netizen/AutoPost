package com.autopost;
import twitter4j.*; import twitter4j.auth.AccessToken; import java.nio.file.Path;
public class TwitterService {
  private final Config cfg; public TwitterService(Config cfg){ this.cfg=cfg; }
  public boolean hasKeys(){ return !(e(cfg.xApiKey())||e(cfg.xApiSecret())||e(cfg.xAccessToken())||e(cfg.xAccessSecret())); }
  private static boolean e(String s){ return s==null||s.isBlank(); }
  public String tweetVideo(String text, Path media) throws TwitterException {
    Twitter t=TwitterFactory.getSingleton(); t.setOAuthConsumer(cfg.xApiKey(), cfg.xApiSecret()); t.setOAuthAccessToken(new AccessToken(cfg.xAccessToken(), cfg.xAccessSecret()));
    try (java.io.FileInputStream fis = new java.io.FileInputStream(media.toFile())) {
      UploadedMedia m=t.uploadMediaChunked(media.getFileName().toString(), fis); StatusUpdate up=new StatusUpdate(text); up.setMediaIds(m.getMediaId()); Status s=t.updateStatus(up);
      return "https://x.com/"+s.getUser().getScreenName()+"/status/"+s.getId();
    } catch (java.io.IOException e) {
      throw new TwitterException("Failed to read media file", e);
    }
  }
}
