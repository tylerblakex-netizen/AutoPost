package com.autopost;
public record Config(String openaiKey,String openaiModel,String rawFolderId,String editsFolderId,String webhookUrl,
                     String saPath,String saInlineJson,String twApiKey,String twApiSecret,String twAccessToken,String twAccessSecret,
                     String servicePublicId,String serviceSecretKey){
  public static Config loadFromEnv(){
    return new Config(req("OPENAI_API_KEY"), env("OPENAI_MODEL","gpt-4o-mini"),
      req("RAW_FOLDER_ID"), req("EDITS_FOLDER_ID"), env("WEBHOOK_URL",""),
      env("GOOGLE_APPLICATION_CREDENTIALS",""), env("GOOGLE_SERVICE_ACCOUNT_JSON",""),
      env("TWITTER_API_KEY",""), env("TWITTER_API_SECRET",""), env("TWITTER_ACCESS_TOKEN",""), env("TWITTER_ACCESS_SECRET",""),
      req("SERVICE_PUBLIC_ID"), req("SERVICE_SECRET_KEY"));
  }
  static String env(String k,String d){ var v=System.getenv(k); return v==null||v.isBlank()?d:v; }
  static String req(String k){ var v=System.getenv(k); if(v==null||v.isBlank()) throw new RuntimeException(k+" is required"); return v; }
  public boolean hasInlineSA(){ return saInlineJson()!=null && !saInlineJson().isBlank(); }
  public boolean hasSAPath(){ return saPath()!=null && !saPath().isBlank(); }
}
