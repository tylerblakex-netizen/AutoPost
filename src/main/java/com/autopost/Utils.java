package com.autopost;
import com.fasterxml.jackson.databind.ObjectMapper; import java.io.InputStream; import java.util.*;
public class Utils {
  private static final ObjectMapper M=new ObjectMapper(); private static final Set<String> GENERIC=Set.of("teaser","clip","video");
  
  public static int maxPostLen(){
    String env=System.getenv("X_MAX_LEN");
    int len=280; // default
    if(env!=null && !env.isBlank()){
      try{ len=Integer.parseInt(env.trim()); }catch(NumberFormatException e){}
    }
    return Math.max(140, Math.min(25000, len)); // bounded 140..25000
  }
  
  public static String ellipsize(String text, int maxLen){
    if(text==null || text.length()<=maxLen) return text;
    return maxLen<=3? text.substring(0,maxLen): text.substring(0,maxLen-3)+"...";
  }
  
  public static String clampToMaxLen(String text){
    return ellipsize(text, maxPostLen());
  }
  
  public static String parseCollabFromFilename(String name){ String base=name.replaceAll("\\.[^.]*$",""); String[] parts=base.split("[-_]",2); String token=parts[0].trim(); return token.isBlank()||GENERIC.contains(token.toLowerCase())? null: token; }
  public static String loadCollabHandle(String collab){ if(collab==null) return null; try(InputStream is=Utils.class.getResourceAsStream("/collabs.json")){ Map<?,?> map=M.readValue(is,Map.class); Object v=map.get(collab); return v==null? null: v.toString(); }catch(Exception e){ return null; } }
  
  public static String joinCaption(String caption, java.util.List<String> tags, String handle){
    int maxLen=maxPostLen();
    String cleanCaption=caption==null? "": caption.trim();
    String cleanHandle=handle!=null && !handle.isBlank()? " "+handle.trim(): "";
    
    // Build hashtags part (limit to 3)
    StringBuilder hashtagsPart=new StringBuilder();
    if(tags!=null && !tags.isEmpty()){
      hashtagsPart.append("\n");
      int n=0;
      for(String h: tags){
        if(n++>=3) break;
        if(h==null||h.isBlank()) continue;
        hashtagsPart.append('#').append(h.replace("#","").trim()).append(' ');
      }
    }
    String hashtagsStr=hashtagsPart.toString().trim();
    
    // Calculate space available for caption
    int reservedLen=cleanHandle.length() + hashtagsStr.length();
    int availableForCaption=maxLen - reservedLen;
    
    // Ensure we have at least some space for caption
    if(availableForCaption<10 && !cleanCaption.isEmpty()){
      // If very little space, prioritize caption over some hashtags
      availableForCaption=Math.max(10, maxLen/2);
      // Recalculate hashtags with less space
      int hashtagBudget=maxLen - availableForCaption - cleanHandle.length();
      if(hashtagBudget<10) hashtagsStr=""; // Drop hashtags if no space
      else hashtagsStr=ellipsize(hashtagsStr, hashtagBudget);
    }
    
    // Clamp caption to available space
    String finalCaption=ellipsize(cleanCaption, Math.max(0, availableForCaption));
    
    // Assemble final text
    String text=finalCaption + cleanHandle;
    if(!hashtagsStr.isEmpty()) text=(text+"\n"+hashtagsStr).trim();
    
    return ellipsize(text, maxLen);
  }
}
