package com.autopost;
public record Config(
    String openaiKey,
    String openaiModel,
    String rawFolderId,
    String editsFolderId,
    String webhookUrl,
    String saPath,
    String saInlineJson,
    String xApiKey,
    String xApiSecret,
    String xAccessToken,
    String xAccessSecret,
    String servicePublicId,
    String serviceSecretKey
) {
  public static Config loadFromEnv(){
   