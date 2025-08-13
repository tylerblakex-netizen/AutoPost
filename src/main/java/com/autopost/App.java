package com.autopost;

import java.util.logging.Logger;

public class App {
  private static final Logger log = Logger.getLogger(App.class.getName());
  
  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      String command = args[0].toLowerCase();
      
      switch (command) {
        case "analyze":
          log.info("Starting Twitter analysis mode...");
          try {
            new XAnalyzer().run();
          } catch (Exception e) {
            log.severe("Analysis failed: " + e.getMessage());
            System.exit(1);
          }
          break;
          
        case "server":
          log.info("Starting Spring Boot server mode...");
          AutoPostApplication.main(args);
          break;
          
        case "help":
        case "--help":
        case "-h":
          printHelp();
          break;
          
        case "check":
          log.info("Checking system dependencies...");
          DependencyChecker.logSystemInfo();
          break;
          
        default:
          log.warning("Unknown command: " + command);
          printHelp();
          System.exit(1);
      }
    } else {
      log.info("Starting default posting mode...");
      try {
        new Runner().run();
      } catch (Exception e) {
        log.severe("Posting failed: " + e.getMessage());
        log.info("Run with 'check' command to verify dependencies");
        System.exit(1);
      }
    }
  }
  
  private static void printHelp() {
    System.out.println("AutoPost - Automated Video Processing and Social Media Posting");
    System.out.println("");
    System.out.println("Usage: java -jar autopost.jar [command]");
    System.out.println("");
    System.out.println("Commands:");
    System.out.println("  (none)    Run main posting workflow (requires all credentials)");
    System.out.println("  analyze   Analyze Twitter posting performance to find optimal times");
    System.out.println("  server    Run as Spring Boot web server with REST API");
    System.out.println("  check     Check system dependencies and configuration");
    System.out.println("  help      Show this help message");
    System.out.println("");
    System.out.println("Environment Variables:");
    System.out.println("  OPENAI_API_KEY           OpenAI API key (required)");
    System.out.println("  RAW_FOLDER_ID           Google Drive folder ID for raw videos (required)");
    System.out.println("  EDITS_FOLDER_ID         Google Drive folder ID for processed videos (required)");
    System.out.println("  GOOGLE_APPLICATION_CREDENTIALS  Path to service account JSON file");
    System.out.println("  GOOGLE_SERVICE_ACCOUNT_JSON      Service account JSON content");
    System.out.println("  TWITTER_API_KEY         Twitter API credentials (optional)");
    System.out.println("  WEBHOOK_URL             Alternative to Twitter for posting (optional)");
    System.out.println("");
    System.out.println("For more information, see README.md");
  }
}
