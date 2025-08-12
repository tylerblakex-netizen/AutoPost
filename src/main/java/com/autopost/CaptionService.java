package com.autopost;
import com.fasterxml.jackson.databind.*; import com.fasterxml.jackson.databind.node.*; import okhttp3.*; import java.util.*; import java.util.regex.*;
public class CaptionService {
  public record Caption(String caption, List<String> hashtags) {}
  private final String apiKey, model; private static final ObjectMapper M=new ObjectMapper(); private static final OkHttpClient HTTP=new OkHttpClient();
  private static final String SYSTEM="You write short, algorithm-friendly social captions. Non-explicit, confident. Output strict JSON: {caption, hashtags} where hashtags is an array (<=3).";
  private static final String PROMPT="""
Create a caption for a short teaser video.
Rules:
- ≤ 1 sentence + optional emoji.
- Avoid explicit sexual language.
- Up to 3 hashtags (return as array; no more).
- Tone: bold, confident, high-energy, non-cringe.
Context:
- Filename/Title: %s
- Collaborator: %s
- Platform: X (but can be reused elsewhere)
""";
  public CaptionService(Config cfg){ this.apiKey=cfg.openaiKey(); this.model=cfg.openaiModel(); }
  public Caption generate(String title, String collaborator) throws Exception{
    String user=PROMPT.formatted(title, collaborator==null? "none": collaborator);
    ObjectNode body=M.createObjectNode(); body.put("model",model);
    ArrayNode msgs=body.putArray("messages"); msgs.addObject().put("role","system").put("content",SYSTEM); msgs.addObject().put("role","user").put("content",user);
    body.put("temperature",0.7);
    Request req=new Request.Builder().url("https://api.openai.com/v1/chat/completions").header("Authorization","Bearer "+apiKey)
      .post(RequestBody.create(M.writeValueAsBytes(body), MediaType.parse("application/json"))).build();
    try(Response resp=HTTP.newCall(req).execute()){
      if(!resp.isSuccessful()) throw new RuntimeException("OpenAI error: "+resp.code()+" "+resp.message());
      var root=M.readTree(resp.body().bytes()); String text=root.at("/choices/0/message/content").asText().trim();
      var m=Pattern.compile("\\{[\\s\\S]*\\}").matcher(text); String json=m.find()? m.group(): text; var p=M.readTree(json);
      java.util.List<String> tags=new java.util.ArrayList<>(); if(p.has("hashtags") && p.get("hashtags").isArray()) for(JsonNode t: p.get("hashtags")){ if(tags.size()>=3) break; tags.add(t.asText().replace("#","").trim()); }
      return new Caption(p.get("caption").asText(), tags);
    }
  }
}
