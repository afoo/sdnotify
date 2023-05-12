package de.afoo.sdnotify;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.newsclub.net.unix.AFUNIXDatagramSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class ActualSDNotifyTest {

  @SystemStub
  private static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  private Server server;
  private File socketFile;

  private ActualSDNotify sdNotify;

  @BeforeAll
  static void beforeAll() {
    boolean isWindows = System.getProperty("os.name", "").startsWith("Windows");
    if (isWindows) {
      throw new RuntimeException("SDNotify tests do not work on Windows");
    }
  }

  @BeforeEach
  void beforeEach(@TempDir File tempDir) throws IOException {
    socketFile = new File(tempDir, "sock");
    server = new Server(socketFile);
    server.run();
    environmentVariables.set("NOTIFY_SOCKET", socketFile.getAbsolutePath());
    sdNotify = new ActualSDNotify();
  }

  private void testMethod(boolean result, String expectedReply) {
    assertTrue(result);
    try {
      server.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    assertEquals(expectedReply, server.getReceived());
  }

  @Test
  void testReady() {
    testMethod(sdNotify.ready(), "READY=1");
  }

  @Test
  void testStatus() {
    testMethod(sdNotify.status("testing"), "STATUS=testing");
  }

  @Test
  void testReloading() throws InterruptedException {
    assertTrue(sdNotify.reloading());
    server.join();
    String received = server.getReceived();
    assertTrue(received.startsWith("RELOADING=1\nMONOTONIC_USEC="));
  }

  @Test
  void testStopping() {
    testMethod(sdNotify.stopping(), "STOPPING=1");
  }

  @Test
  void testErrno() {
    testMethod(sdNotify.errno(42), "ERRNO=42");
  }

  @Test
  void testBusError() {
    testMethod(sdNotify.busError("test"), "BUSERROR=test");
  }

  @Test
  void testMainPid() {
    testMethod(sdNotify.mainPid(42), "MAINPID=42");
  }

  @Test
  void testWatchdog() {
    testMethod(sdNotify.watchdog(), "WATCHDOG=1");
  }

  @Test
  void testWatchdogTrigger() {
    testMethod(sdNotify.watchdogTrigger(), "WATCHDOG=trigger");
  }

  @Test
  void testExtendTimeout() {
    testMethod(sdNotify.extendTimeout(Duration.ofSeconds(2)), "EXTEND_TIMEOUT_USEC=2000000");
  }

  @Test
  void testSocketMissing() throws InterruptedException {
    environmentVariables.set("NOTIFY_SOCKET", "/does/not/exist");
    assertFalse(sdNotify.ready());
    server.join();
    assertEquals("", server.getReceived());
  }

  @Test
  void testEnvironmentMissing() throws InterruptedException {
    environmentVariables.set("NOTIFY_SOCKET", null);
    assertFalse(sdNotify.ready());
    server.join();
    assertEquals("", server.getReceived());
  }

  @Test
  void testNoListener() throws InterruptedException, IOException {
    server.join();
    assertTrue(socketFile.delete());
    FileUtils.writeLines(socketFile, Collections.singleton("foo"));
    assertFalse(sdNotify.ready());
  }

  private static class Server {
    private final Thread thread;
    private final StringBuilder received = new StringBuilder();

    public int expectedPackets = 1;

    public Server(File socketFile) throws IOException {
      FileUtils.touch(socketFile);
      AFUNIXDatagramSocket sock = AFUNIXDatagramSocket.newInstance();
      sock.setSoTimeout(100);
      sock.bind(AFUNIXSocketAddress.of(socketFile));
      thread =
          new Thread(
              () -> {
                for (int i = 0; i < expectedPackets; i++) {
                  try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, 1024);
                    sock.receive(packet);
                    received.append(new String(buf, StandardCharsets.US_ASCII).trim());
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
                sock.close();
              });
    }

    public void run() {
      thread.start();
    }

    public void join() throws InterruptedException {
      thread.join();
    }

    public String getReceived() {
      return received.toString();
    }
  }
}
