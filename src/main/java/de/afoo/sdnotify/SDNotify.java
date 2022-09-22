package de.afoo.sdnotify;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

@Slf4j
public class SDNotify {

  private SDNotify() {}

  private static void send(String message) {
    try (AFUNIXDatagramSocket sock = AFUNIXDatagramSocket.newInstance()) {
      AFUNIXSocketAddress addr = AFUNIXSocketAddress.of(new File(System.getenv("NOTIFY_SOCKET")));
      sock.connect(addr);
      sock.send(new DatagramPacket(message.getBytes(), message.length()));
    } catch (IOException e) {
      log.error("Could not send SD_NOTIFY message {}: {}", message, e.getMessage(), e);
    }
  }

  public static void ready() {
    send("READY=1");
  }

  public static void status(String message) {
    send("STATUS=%s".formatted(message));
  }

  public static void reloading() {
    send("RELOADING=1");
  }

  public static void stopping() {
    send("STOPPING=1");
  }

  public static void errno(int errno) {
    send("ERRNO=%d".formatted(errno));
  }

  public static void bussError(String error) {
    send("BUSERROR=%s".formatted(error));
  }

  public static void mainPid(int pid) {
    send("MAINPID=%d".formatted(pid));
  }

  public static void watchdog() {
    send("WATCHDOG=1");
  }
}
