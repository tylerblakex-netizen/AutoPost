package com.autopost;
import java.text.Normalizer; import java.time.LocalDate; import java.time.format.DateTimeFormatter;
public class FilenameUtil {
  private static final DateTimeFormatter D=DateTimeFormatter.ofPattern("yyyyMMdd");
  public static String sanitizeBase(String s){
    String n=Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
    n=n.replaceAll("[^A-Za-z0-9._-]+","_").replaceAll("_+","_").replaceAll("(^_|_$)","");
    if(n.length()>60) n=n.substring(0,60); if(!n.toLowerCase().endsWith(".mp4")) n+=".mp4"; return n;
  }
  public static String buildName(String collab,String type,int i){
    String date=LocalDate.now().format(D); String base=(collab==null||collab.isBlank()? "clip": collab)+"_"+type+"_"+String.format("%02d",i);
    return sanitizeBase(date+"_"+base+".mp4");
  }
}
