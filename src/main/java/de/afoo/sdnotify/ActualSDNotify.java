package de.afoo.sdnotify;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This implementation of <code>SDNotify</code> actually tries to talk to systemd. */
public class ActualSDNotify implements SDNotify {
  private static final Logger log = LoggerFactory.getLogger(SDNotify.class);

  private boolean send(String message) {
    File notifySocketFile = SDNotifySocketFile.get();
    if (notifySocketFile == null) {
      log.error("Could not send SD_NOTIFY message: NOTIFY_SOCKET environment variable not set.");
      return false;
    }
    if (!notifySocketFile.exists()) {
      log.error(
          "Could not send SD_NOTIFY message: {} does not exist.",
          notifySocketFile.getAbsolutePath());
      return false;
    }
    try (AFUNIXDatagramSocket sock = AFUNIXDatagramSocket.newInstance()) {
      AFUNIXSocketAddress addr = AFUNIXSocketAddress.of(notifySocketFile);
      sock.connect(addr);
      sock.send(new DatagramPacket(message.getBytes(), message.length()));
      return true;
    } catch (IOException e) {
      log.error("Could not send SD_NOTIFY message {}: {}", message, e.getMessage(), e);
      return false;
    }
  }

  private long getMicroseconds(Duration duration) {
    return TimeUnit.NANOSECONDS.toMicros(duration.toNanos());
  }

  @Override
  public boolean ready() {
    return send("READY=1");
  }

  @Override
  public boolean status(String message) {
    return send(String.format("STATUS=%s", message));
  }

  @Override
  public boolean reloading() {
    return send(String.format("RELOADING=1\nMONOTONIC_USEC=%d", System.nanoTime() / 1000));
  }

  @Override
  public boolean stopping() {
    return send("STOPPING=1");
  }

  @Override
  public boolean errno(int errno) {
    return send(String.format("ERRNO=%d", errno));
  }

  @Override
  public boolean busError(String error) {
    return send(String.format("BUSERROR=%s", error));
  }

  @Override
  public boolean mainPid(int pid) {
    return send(String.format("MAINPID=%d", pid));
  }

  @Override
  public boolean watchdog() {
    return send("WATCHDOG=1");
  }

  @Override
  public boolean watchdogTrigger() {
    return send("WATCHDOG=trigger");
  }

  @Override
  public boolean extendTimeout(Duration duration) {
    return send(String.format("EXTEND_TIMEOUT_USEC=%d", getMicroseconds(duration)));
  }
}
