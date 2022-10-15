/**
 * A simple interface to <a
 * href="https://www.freedesktop.org/software/systemd/man/sd_notify.html">sd_notify</a> from Java.
 *
 * <p>This does not use the systemd provided C library or commandline tool but instead directly
 * talks to the UNIX socket systemd provides.
 *
 * <p>In most cases you should use {@link de.afoo.sdnotify.SDNotify#create()} to get an instance.
 */
package de.afoo.sdnotify;
