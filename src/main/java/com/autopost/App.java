package com.autopost;
public class App {
  public static void main(String[] args) throws Exception {
    if (args.length>0 && args[0].equalsIgnoreCase("analyze")) {
      new XAnalyzer().run();
    } else if (args.length>0 && args[0].equalsIgnoreCase("server")) {
      AutoPostApplication.main(args);
    } else {
      new Runner().run();
    }
  }
}
