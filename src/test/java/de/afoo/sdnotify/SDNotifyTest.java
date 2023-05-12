package de.afoo.sdnotify;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class SDNotifyTest {
  @SystemStub
  private static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Test
  void testCreate(@TempDir File tempDir) throws IOException {
    setSocket(null);
    assertInstanceOf(NOOPSDNotify.class, SDNotify.create());

    setSocket("foo");
    assertInstanceOf(NOOPSDNotify.class, SDNotify.create());

    File file = new File(tempDir, "foo");
    setSocket(file.getAbsolutePath());
    assertTrue(file.createNewFile());
    assertInstanceOf(ActualSDNotify.class, SDNotify.create());
  }

  private void setSocket(String value) {
    environmentVariables.set("NOTIFY_SOCKET", value);
  }
}
