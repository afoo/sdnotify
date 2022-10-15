package de.afoo.sdnotify.testservice;

import de.afoo.sdnotify.SDNotify;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {
  public static void main(String... args) throws InterruptedException, IOException {
    log.info("SOCKET TARGET: {}", System.getenv("NOTIFY_SOCKET"));
    var sdNotify = SDNotify.create();
    sdNotify.ready();
    var flip = true;
    for (int i = 0; i <= 15; i++) {
      sdNotify.status(flip ? "derping" : "herping");
      flip = !flip;
      Thread.sleep(500);
    }
    sdNotify.status("almost done");
    sdNotify.stopping();
    Thread.sleep(5000);
    Thread.sleep(5000);
  }
}
