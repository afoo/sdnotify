# SDNotify Example Service

This application will

1. take 5 seconds to start up before sending READY to systemd.
2. then take another 5 seconds being "ready but not quite".
3. and finally just silently wait until stopped, taking another 5 seconds before exiting.
4. communicate all these steps through STATUS messages to systemd.

## Usage

To build the application and install it as a user service, run 

```shell
./gradlew installService
```

To observe the service's actions, start something like 

```shell
watch -n 1 systemctl --user status sdnotify-example
``` 

in one terminal, while in another doing

```shell
systemctl --user daemon-reload
systemctl --user start sdnotify-example

# Some time later...
systemctl --user stop sdnotify-example
```