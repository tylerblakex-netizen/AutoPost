package com.autopost;
public class App {
  public static void main(String[] args) throws Exception {
    if (args.length>0 && args[0].equalsIgnoreCase("analyze")) new XAnalyzer().run();
    else new Runner().run();
  }
}
