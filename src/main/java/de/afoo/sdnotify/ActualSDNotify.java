package de.afoo.sdnotify;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.IOException;
import java.net.DatagramPacket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Collection of static methods to send Notifications to systemd.
 *
 * <p>See <a
 * href="https://www.freedesktop.org/software/systemd/man/sd_notify.html">https://www.freedesktop.org/software/systemd/man/sd_notify.html</a>.
 */
@Slf4j
public class ActualSDNotify implements SDNotify {

  private boolean send(String message) {
    var notifySocketFile = SDNotifySocketFile.get();
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
      var addr = AFUNIXSocketAddress.of(notifySocketFile);
      sock.connect(addr);
      sock.send(new DatagramPacket(message.getBytes(), message.length()));
      return true;
    } catch (IOException e) {
      log.error("Could not send SD_NOTIFY message {}: {}", message, e.getMessage(), e);
      return false;
    }
  }

  private long getMicroseconds(Duration duration) {
    return duration.dividedBy(ChronoUnit.MICROS.getDuration());
  }

  @Override
  public boolean ready() {
    return send("READY=1");
  }

  @Override
  public boolean status(String message) {
    return send("STATUS=%s".formatted(message));
  }

  @Override
  public boolean reloading() {
    return send("RELOADING=1");
  }

  @Override
  public boolean stopping() {
    return send("STOPPING=1");
  }

  @Override
  public boolean errno(int errno) {
    return send("ERRNO=%d".formatted(errno));
  }

  @Override
  public boolean busError(String error) {
    return send("BUSERROR=%s".formatted(error));
  }

  @Override
  public boolean mainPid(int pid) {
    return send("MAINPID=%d".formatted(pid));
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
    return send("EXTEND_TIMEOUT_USEC=%d".formatted(getMicroseconds(duration)));
  }
}
