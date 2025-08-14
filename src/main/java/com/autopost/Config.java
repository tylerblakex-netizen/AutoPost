package com.autopost;
import java.util.Map;
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
    String twAccessSecret
) {
  public static Config loadFromEnv(){
    return loadFromEnv(System.getenv());
  }
  
  public static Config loadFromEnv(Map<String, String> envVars){
    return new Config(req(envVars, "OPENAI_API_KEY"), env(envVars, "OPENAI_MODEL","gpt-4o-mini"),
      req(envVars, "RAW_FOLDER_ID"), req(envVars, "EDITS_FOLDER_ID"), env(envVars, "WEBHOOK_URL",""),
      env(envVars, "GOOGLE_APPLICATION_CREDENTIALS",""), env(envVars, "GOOGLE_SERVICE_ACCOUNT_JSON",""),
      env(envVars, "TWITTER_API_KEY",""), env(envVars, "TWITTER_API_SECRET",""), env(envVars, "TWITTER_ACCESS_TOKEN",""), env(envVars, "TWITTER_ACCESS_SECRET",""));
  }
  
  static String env(String k,String d){ var v=System.getenv(k); return v==null||v.isBlank()?d:v; }
  static String req(String k){ var v=System.getenv(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  
  static String env(Map<String, String> envVars, String k, String d){ var v=envVars.get(k); return v==null||v.isBlank()?d:v; }
  static String req(Map<String, String> envVars, String k){ var v=envVars.get(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  public boolean hasInlineSA(){ return saInlineJson()!=null && !saInlineJson().isBlank(); }
  public boolean hasSAPath(){ return saPath()!=null && !saPath().isBlank(); }
}
