package de.afoo.sdnotify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SystemStubsExtension.class)
class SDNotifyTest {

    @SystemStub
    private static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeAll
    static void beforeAll() {
        environmentVariables.set("NOTIFY_SOCKET", "/tmp/test");
    }

    @Test
    void testEnv() {
        assertEquals("/tmp/test", System.getenv("NOTIFY_SOCKET"));
    }
}
