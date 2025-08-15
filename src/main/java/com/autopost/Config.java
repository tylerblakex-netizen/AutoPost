package com.autopost;
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
    return loadFromEnv(System.getenv());
  }
  
  public static Config loadFromEnv(java.util.Map<String, String> envVars){
    return new Config(req("OPENAI_API_KEY", envVars), env("OPENAI_MODEL","gpt-4o-mini", envVars),
      req("RAW_FOLDER_ID", envVars), req("EDITS_FOLDER_ID", envVars), env("WEBHOOK_URL","", envVars),
      env("GOOGLE_APPLICATION_CREDENTIALS","", envVars), env("GOOGLE_SERVICE_ACCOUNT_JSON","", envVars),
      env("TWITTER_API_KEY","", envVars), env("TWITTER_API_SECRET","", envVars), env("TWITTER_ACCESS_TOKEN","", envVars), env("X_ACCESS_TOKEN_SECRET","", envVars),
      req("SERVICE_PUBLIC_ID", envVars), req("SERVICE_SECRET_KEY", envVars));
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
  static String env(String k,String d, java.util.Map<String, String> envVars){ var v=envVars.get(k); return v==null||v.isBlank()?d:v; }
  static String req(String k, java.util.Map<String, String> envVars){ var v=envVars.get(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  static String propWithDefault(String k,String d){ var v=System.getProperty(k); return v==null||v.isBlank()?d:v; }
  static String reqProp(String k){ var v=System.getProperty(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  public boolean hasInlineSA(){ return saInlineJson()!=null && !saInlineJson().isBlank(); }
  public boolean hasSAPath(){ return saPath()!=null && !saPath().isBlank(); }
}
