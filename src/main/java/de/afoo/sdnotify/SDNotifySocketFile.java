package de.afoo.sdnotify;

import java.io.File;

public class SDNotifySocketFile {

  private SDNotifySocketFile() {}

  public static File get() {
    var env = System.getenv("NOTIFY_SOCKET");
    if (env == null) {
      return null;
    }
    return new File(env);
  }

  public static boolean exists() {
    var f = get();
    return f != null && f.exists();
  }
}
