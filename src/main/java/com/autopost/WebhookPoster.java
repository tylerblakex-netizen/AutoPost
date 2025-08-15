package com.autopost;
import java.net.http.*; import java.net.URI; import java.time.Duration; import com.fasterxml.jackson.databind.ObjectMapper;
public class WebhookPoster {
  private final String url; private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private static final ObjectMapper M=new ObjectMapper(); public WebhookPoster(Config cfg){ this.url=null; } // webhookUrl removed from Config
  public void post(Object payload) throws Exception{ if(url==null||url.isBlank()) return;
    byte[] body=M.writeValueAsBytes(payload); var req=HttpRequest.newBuilder(URI.create(url)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
    var res=HTTP.send(req, HttpResponse.BodyHandlers.ofString()); if(res.statusCode()>=300) throw new RuntimeException("Webhook failed: "+res.statusCode()+" "+res.body());
  }
}
