package de.afoo.sdnotify;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class SDNotifyTest {

  @SystemStub
  private static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  private Server server;
  private File socketFile;

  @BeforeEach
  void beforeEach(@TempDir File tempDir) throws IOException {
    socketFile = new File(tempDir, "sock");
    server = new Server(socketFile);
    server.run();
    environmentVariables.set("NOTIFY_SOCKET", socketFile.getAbsolutePath());
  }

  @SneakyThrows
  private void testMethod(boolean result, String expectedReply) {
    assertTrue(result);
    server.join();
    assertEquals(expectedReply, server.received);
  }

  @Test
  void testReady() {
    testMethod(SDNotify.ready(), "READY=1");
  }

  @Test
  void testStatus() {
    testMethod(SDNotify.status("testing"), "STATUS=testing");
  }

  @Test
  void testReloading() {
    testMethod(SDNotify.reloading(), "RELOADING=1");
  }

  @Test
  void testStopping() {
    testMethod(SDNotify.stopping(), "STOPPING=1");
  }

  @Test
  void testErrno() {
    testMethod(SDNotify.errno(42), "ERRNO=42");
  }

  @Test
  void testBusError() {
    testMethod(SDNotify.busError("test"), "BUSERROR=test");
  }

  @Test
  void testMainPid() {
    testMethod(SDNotify.mainPid(42), "MAINPID=42");
  }

  @Test
  void testWatchdog() {
    testMethod(SDNotify.watchdog(), "WATCHDOG=1");
  }

  @Test
  void testSocketMissing() throws InterruptedException {
      environmentVariables.set("NOTIFY_SOCKET", "/does/not/exist");
      assertFalse(SDNotify.ready());
      server.join();
      assertNull(server.received);
  }

  @Test
  void testEnvironmentMissing() throws InterruptedException {
    environmentVariables.set("NOTIFY_SOCKET", null);
    assertFalse(SDNotify.ready());
    server.join();
    assertNull(server.received);
  }

  @Test
  void testNoListener() throws InterruptedException, IOException {
    server.join();
    assertTrue(socketFile.delete());
    FileUtils.writeLines(socketFile, Collections.singleton("foo"));
    assertFalse(SDNotify.ready());
  }

  private static class Server {
    private final AFUNIXDatagramSocket sock;
    private final Thread thread;
    private String received;

    public Server(File socketFile) throws IOException {
      FileUtils.touch(socketFile);
      sock = AFUNIXDatagramSocket.newInstance();
      sock.setSoTimeout(1000);
      sock.bind(AFUNIXSocketAddress.of(socketFile));
      thread =
          new Thread(
              () -> {
                var buf = new byte[1024];
                var pack = new DatagramPacket(buf, 1024);
                try {
                  sock.receive(pack);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
                received = new String(buf, StandardCharsets.US_ASCII).trim();
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
