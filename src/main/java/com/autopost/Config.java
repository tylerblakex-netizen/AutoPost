package com.autopost;
import com.fasterxml.jackson.databind.ObjectMapper;
public record Config(
    String openaiKey,
    String openaiModel,
    String rawFolderId,
    String editsFolderId,
    String webhookUrl,
    String saPath,
    String saInlineJson,
    String twApiKey,
    String twApiSecret,
    String twAccessToken,
    String twAccessSecret,
    String servicePublicId,
    String serviceSecretKey
) {
  public static Config loadFromEnv(){
    return new Config(req("OPENAI_API_KEY"), env("OPENAI_MODEL","gpt-4o-mini"),
      req("RAW_FOLDER_ID"), req("EDITS_FOLDER_ID"), env("WEBHOOK_URL",""),
      env("GOOGLE_APPLICATION_CREDENTIALS",""), env("GOOGLE_SERVICE_ACCOUNT_JSON",""),
      env("TWITTER_API_KEY",""), env("TWITTER_API_SECRET",""), env("TWITTER_ACCESS_TOKEN",""), env("X_ACCESS_TOKEN_SECRET",""),
      req("SERVICE_PUBLIC_ID"), req("SERVICE_SECRET_KEY"));
  }
  
  public static Config loadFromSystemProperties(){
    return new Config(reqProp("OPENAI_API_KEY"), propWithDefault("OPENAI_MODEL","gpt-4o-mini"),
      reqProp("RAW_FOLDER_ID"), reqProp("EDITS_FOLDER_ID"), propWithDefault("WEBHOOK_URL",""),
      propWithDefault("GOOGLE_APPLICATION_CREDENTIALS",""), propWithDefault("GOOGLE_SERVICE_ACCOUNT_JSON",""),
      propWithDefault("TWITTER_API_KEY",""), propWithDefault("TWITTER_API_SECRET",""), propWithDefault("TWITTER_ACCESS_TOKEN",""), propWithDefault("X_ACCESS_TOKEN_SECRET",""),
      reqProp("SERVICE_PUBLIC_ID"), reqProp("SERVICE_SECRET_KEY"));
  }
  static String env(String k,String d){ var v=System.getenv(k); return v==null||v.isBlank()?d:v; }
  static String req(String k){ var v=System.getenv(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  static String propWithDefault(String k,String d){ var v=System.getProperty(k); return v==null||v.isBlank()?d:v; }
  static String reqProp(String k){ var v=System.getProperty(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  
  private static boolean isValidJson(String json) {
    if (json == null || json.isBlank()) return false;
    try {
      new ObjectMapper().readTree(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  public boolean hasInlineSA(){ 
    if (saInlineJson() == null || saInlineJson().isBlank()) return false;
    if (!isValidJson(saInlineJson())) {
      throw new RuntimeException("GOOGLE_SERVICE_ACCOUNT_JSON contains invalid JSON format");
    }
    return true;
  }
  public boolean hasSAPath(){ return saPath()!=null && !saPath().isBlank(); }
}
