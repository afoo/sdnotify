package de.afoo.sdnotify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NOOPSDNotifyTest {
    private NOOPSDNotify sdNotify;

    @BeforeEach
    void beforeEach() {
        sdNotify = new NOOPSDNotify();
    }

    @Test
    void testReady() {
        assertTrue(sdNotify.ready());
    }

    @Test
    void testStatus() {
        assertTrue(sdNotify.status("testing"));
    }

    @Test
    void testReloading() {
        assertTrue(sdNotify.reloading());
    }

    @Test
    void testStopping() {
        assertTrue(sdNotify.stopping());
    }

    @Test
    void testErrno() {
        assertTrue(sdNotify.errno(42));
    }

    @Test
    void testBusError() {
        assertTrue(sdNotify.busError("test"));
    }

    @Test
    void testMainPid() {
        assertTrue(sdNotify.mainPid(42));
    }

    @Test
    void testWatchdog() {
        assertTrue(sdNotify.watchdog());
    }

    @Test
    void testWatchdogTrigger() {
        assertTrue(sdNotify.watchdogTrigger());
    }

    @Test
    void testExtendTimeout() {
        assertTrue(sdNotify.extendTimeout(Duration.ofSeconds(2)));
    }
}
