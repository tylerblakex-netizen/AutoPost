package com.autopost;
import twitter4j.*; import twitter4j.auth.AccessToken; import java.nio.file.Path;
public class TwitterService {
  private final Config cfg; public TwitterService(Config cfg){ this.cfg=cfg; }
  public boolean hasKeys(){ return !(e(cfg.twApiKey())||e(cfg.twApiSecret())||e(cfg.twAccessToken())||e(cfg.twAccessSecret())); }
  private static boolean e(String s){ return s==null||s.isBlank(); }
  public String tweetVideo(String text, Path media) throws TwitterException {
    Twitter t=TwitterFactory.getSingleton(); t.setOAuthConsumer(cfg.twApiKey(), cfg.twApiSecret()); t.setOAuthAccessToken(new AccessToken(cfg.twAccessToken(), cfg.twAccessSecret()));
    UploadedMedia m=t.uploadMediaChunked(media.toFile()); StatusUpdate up=new StatusUpdate(text); up.setMediaIds(m.getMediaId()); Status s=t.updateStatus(up);
    return "https://x.com/"+s.getUser().getScreenName()+"/status/"+s.getId();
  }
}
