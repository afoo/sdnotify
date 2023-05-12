package de.afoo.sdnotifyexample;

import de.afoo.sdnotify.SDNotify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);
  private static final SDNotify sdNotify = SDNotify.create();

  public static void main(String[] args) {
    // set a status informing the world that we're starting up
    setStatus("starting up");

    // set up handlers for SIGTERM and SIGHUP
    setupSignalHandlers();

    // just wait a while so this is easier to observe in `systemctl status`
    sleep(3);

    // then finally tell systemd we're ready.
    // a `systemctl start` will only return after we do this!
    sdNotify.ready();

    // we have to do this at least every WatchdogSec seconds (see .service file)
    sdNotify.watchdog();

    setStatus("ready to serve but still initializing some things");
    sleep(3);
    sdNotify.watchdog();
    setStatus("truly ready");

    //noinspection InfiniteLoopStatement
    while (true) {
      sleep(3);
      sdNotify.watchdog();
    }
  }

  private static void setupSignalHandlers() {
    // when we receive SIGTERM (systemctl stop for example)…
    Signal.handle(
        new Signal("TERM"),
        (signal) -> {
          // …we update the status,…
          setStatus("shutting down");

          // …inform systemd we're in the process of stopping,…
          sdNotify.stopping();

          // …wait for a while…
          sleep(5);

          // …then set one final status before exiting.
          setStatus("stopped");
          System.exit(0);
        });

    // when we receive SIGHUP (systemctl reload on notify-reload services for example)…
    Signal.handle(
        new Signal("HUP"),
        (signal) -> {
          // …we again update the status…
          setStatus("reloading");

          // …and tell systemd we're currently reloading…
          sdNotify.reloading();

          // …then we wait a wile…
          sleep(5);

          // …before setting a new status and telling systemd we're ready again.
          setStatus("truly ready (again)");
          sdNotify.ready();
        });
  }

  private static void setStatus(String status) {
    sdNotify.status(status);
    log.info(status);
  }

  private static void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      setStatus("gracefully interrupted");
      System.exit(0);
    }
  }
}
