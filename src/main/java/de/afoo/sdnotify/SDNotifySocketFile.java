package de.afoo.sdnotify;

import java.io.File;

/**
 * Thin abstraction around the UNIX socket file systemd stores in the NOTIFY_SOCKET environment
 * variable of processes it starts.
 */
public class SDNotifySocketFile {

  private SDNotifySocketFile() {}

  /**
   * Returns null if the NOTIFY_SOCKET environment variable is not set (which should mean we are not
   * running under systemd in most cases)
   *
   * @return the UNIX socket or <code>null</code>
   */
  public static File get() {
    String env = System.getenv("NOTIFY_SOCKET");
    if (env == null) {
      return null;
    }
    return new File(env);
  }

  /**
   * Check if the UNIX socket to communicate with systemd exists.
   *
   * @return true if the socket exists
   */
  public static boolean exists() {
    File f = get();
    return f != null && f.exists();
  }
}
