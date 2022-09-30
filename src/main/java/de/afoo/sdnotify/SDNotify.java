package de.afoo.sdnotify;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

/** Collection of static methods to send Notifications to systemd. */
@Slf4j
public class SDNotify {

  private SDNotify() {}

  private static boolean send(String message) {
    var notifySocketFileName = System.getenv("NOTIFY_SOCKET");
    if (notifySocketFileName == null) {
      log.error("Could not send SD_NOTIFY message: NOTIFY_SOCKET environment variable not set.");
      return false;
    }
    var notifySocketFile = new File(notifySocketFileName);
    if (!notifySocketFile.exists()) {
      log.error("Could not send SD_NOTIFY message: {} does not exist.", notifySocketFile.getAbsolutePath());
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

  /**
   * Notify systemd that the service has started successfully. This has to be sent for services of
   * Type=notify to be considered running by systemd.
   *
   * @return if the message was successfully sent
   */
  public static boolean ready() {
    return send("READY=1");
  }

  /**
   * Send a free form status message to systemd.
   *
   * @param message the message to be sent
   * @return if the message was successfully sent
   */
  public static boolean status(String message) {
    return send("STATUS=%s".formatted(message));
  }

  /**
   * Notify systemd that the service is currently reloading. {@link #ready()} must be called once
   * this reloading has finished.
   *
   * @return if the message was successfully sent
   */
  public static boolean reloading() {
    return send("RELOADING=1");
  }

  /**
   * Notify systemd that the service is stopping.
   *
   * @return if the message was successfully sent
   */
  public static boolean stopping() {
    return send("STOPPING=1");
  }

  /**
   * Notify systemd that an error has occurred.
   *
   * @param errno the ERRNO of the error
   * @return if the message was successfully sent
   */
  public static boolean errno(int errno) {
    return send("ERRNO=%d".formatted(errno));
  }

  /**
   * Notify systemd that a D-Bus error has occurred.
   *
   * @param error a description of the error
   * @return if the message was successfully sent
   */
  public static boolean bussError(String error) {
    return send("BUSERROR=%s".formatted(error));
  }

  /**
   * Inform systemd of the main pid of the service. This is only necessary under special
   * circumstances.
   *
   * @param pid the main pic
   * @return if the message was successfully sent
   */
  public static boolean mainPid(int pid) {
    return send("MAINPID=%d".formatted(pid));
  }

  /**
   * Something something watchdog.
   *
   * @return if the message was successfully sent
   */
  public static boolean watchdog() {
    return send("WATCHDOG=1");
  }
}
