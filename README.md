# SDNotify

A simple library for Java 17+ implementing the [sd_notify](https://www.freedesktop.org/software/systemd/man/sd_notify.html) protocol from systemd.

```xml
<dependency>
    <groupId>de.afoo</groupId>
    <artifactId>sdnotify</artifactId>
    <version>1.0-beta7</version>
</dependency>
```

```groovy
implementation 'de.afoo:sdnotify:1.0-beta7'
```

## Usage

Create an instance using 

```java
SDNotify sdNotify = SDNotify.create();
```

This will either create an instance of `ActualSDNotify` or `NOOPSDNotify` depending on the availability of the sd_notify socket file. 
This means this library is usable safely whether you are running a service of `Type=notify` or not. 

Once you have your instance, you can use it for communication between your service and systemd.

```java
// tell systemd that your service is ready
sdNotify.ready();

// set status information for your service
sdNotify.status("currently running cleanup");

// inform systemd that your service is reloading
sdNotify.reloading();
  
// ... or stopping
sdNotify.stopping();

// Tell the watchdog, that your service is still available
// see https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#WatchdogSec=
sdNotify.watchdog();
```

For more comprehensive documentation, see [here](https://sdnotify.afoo.de).