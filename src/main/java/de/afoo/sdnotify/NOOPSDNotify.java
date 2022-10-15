package de.afoo.sdnotify;

import java.time.Duration;

/**
 * NOOP Implementation of the SDNotify interface to be used when we are not running under systemd.
 */
public class NOOPSDNotify implements SDNotify {
  @Override
  public boolean ready() {
    return true;
  }

  @Override
  public boolean status(String message) {
    return true;
  }

  @Override
  public boolean reloading() {
    return true;
  }

  @Override
  public boolean stopping() {
    return true;
  }

  @Override
  public boolean errno(int errno) {
    return true;
  }

  @Override
  public boolean busError(String error) {
    return true;
  }

  @Override
  public boolean mainPid(int pid) {
    return true;
  }

  @Override
  public boolean watchdog() {
    return true;
  }

  @Override
  public boolean watchdogTrigger() {
    return true;
  }

  @Override
  public boolean extendTimeout(Duration duration) {
    return true;
  }
}
