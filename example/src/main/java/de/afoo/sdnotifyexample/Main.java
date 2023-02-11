package de.afoo.sdnotifyexample;

import de.afoo.sdnotify.SDNotify;
import sun.misc.Signal;

public class Main {
  private static final SDNotify sdNotify = SDNotify.create();

  private static void setStatus(String status) {
    sdNotify.status(status);
    System.out.println(status);
  }

  private static void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      sdNotify.status("gracefully interrupted");
      System.exit(0);
    }
  }
  public static void main(String[] args)  {
    setStatus("starting up");
    Signal.handle(new Signal("TERM"), (signal) -> {
      setStatus("shutting down");
      sdNotify.stopping();
      sleep(5);
      sdNotify.status("stopped");
      System.exit(0);
    });
    sleep(5);
    sdNotify.ready();
    setStatus("ready to serve but still initializing some things");
    sleep(5);
    setStatus("truly ready");
    while (true) {
      sleep(10);
    }
  }
}