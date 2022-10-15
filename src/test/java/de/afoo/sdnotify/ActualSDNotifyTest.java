package de.afoo.sdnotify;

import lombok.SneakyThrows;
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

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class ActualSDNotifyTest {

  @SystemStub
  private static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  private Server server;
  private File socketFile;

  private ActualSDNotify sdNotify;

  @BeforeAll
  static void beforeAll() {
    var isWindows = System.getProperty("os.name", "").startsWith("Windows");
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

  @SneakyThrows
  private void testMethod(boolean result, String expectedReply) {
    assertTrue(result);
    server.join();
    assertEquals(expectedReply, server.received);
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
  void testReloading() {
    testMethod(sdNotify.reloading(), "RELOADING=1");
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
    assertNull(server.received);
  }

  @Test
  void testEnvironmentMissing() throws InterruptedException {
    environmentVariables.set("NOTIFY_SOCKET", null);
    assertFalse(sdNotify.ready());
    server.join();
    assertNull(server.received);
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
    private String received;

    public Server(File socketFile) throws IOException {
      FileUtils.touch(socketFile);
      var sock = AFUNIXDatagramSocket.newInstance();
      sock.setSoTimeout(100);
      sock.bind(AFUNIXSocketAddress.of(socketFile));
      thread =
          new Thread(
              () -> {
                try {
                  var buf = new byte[1024];
                  var packet = new DatagramPacket(buf, 1024);
                  sock.receive(packet);
                  received = new String(buf, StandardCharsets.US_ASCII).trim();
                  sock.close();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }

    public void run() {
      thread.start();
    }

    public void join() throws InterruptedException {
      thread.join();
    }
  }
}
