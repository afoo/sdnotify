package de.afoo.sdnotify;

import java.time.Duration;

/**
 * Send Notifications to systemd.
 *
 * <p>See <a
 * href="https://www.freedesktop.org/software/systemd/man/sd_notify.html">https://www.freedesktop.org/software/systemd/man/sd_notify.html</a>.
 */
public interface SDNotify {
  /**
   * Create a new SDNotify instance. Returns a NOOP Variant if the <code>NOTIFY_SOCKET</code>
   * environment variable is not set (for example on Windows).
   *
   * @return an appropriate SDNotify instance
   */
  static SDNotify create() {
    if (SDNotifySocketFile.exists()) {
      return new ActualSDNotify();
    }
    return new NOOPSDNotify();
  }

  /**
   * Notify systemd that the service has started successfully. This has to be sent for services of
   * Type=notify to be considered running by systemd.
   *
   * @return true if the message was successfully sent
   */
  boolean ready();

  /**
   * Send a free form status message to systemd.
   *
   * @param message the message to be sent
   * @return true if the message was successfully sent
   */
  boolean status(String message);

  /**
   * Notify systemd that the service is currently reloading. {@link #ready()} must be called once
   * this reloading has finished.
   *
   * @return true if the message was successfully sent
   */
  boolean reloading();

  /**
   * Notify systemd that the service is stopping.
   *
   * @return true if the message was successfully sent
   */
  boolean stopping();

  /**
   * Notify systemd that an error has occurred.
   *
   * @param errno the ERRNO of the error
   * @return true if the message was successfully sent
   */
  boolean errno(int errno);

  /**
   * Notify systemd that a D-Bus error has occurred.
   *
   * @param error a description of the error
   * @return true if the message was successfully sent
   */
  boolean busError(String error);

  /**
   * Inform systemd of the main pid of the service. This is only necessary under special
   * circumstances.
   *
   * @param pid the main pic
   * @return true if the message was successfully sent
   */
  boolean mainPid(int pid);

  /**
   * Update the watchdog timestamp for services with WatchDogSec enabled.
   *
   * @return true if the message was successfully sent
   */
  boolean watchdog();

  /**
   * An error that should be handled by the watchdog options occured.
   *
   * @return true if the message was successfully sent
   */
  boolean watchdogTrigger();

  /**
   * Tells the service manager to extend the startup, runtime or shutdown service timeout
   * corresponding the current state.
   *
   * @param duration the new value of the timeout
   * @return true if the message was successfully sent
   */
  boolean extendTimeout(Duration duration);
}
