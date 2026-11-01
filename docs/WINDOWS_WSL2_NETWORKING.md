# Windows + WSL2 + Android Emulator Networking

## Why `10.0.2.2` is not the whole story

The Android Emulator's `10.0.2.2` address represents the Windows development host's loopback interface. The C++ simulator, however, runs inside the WSL2 Linux environment. WSL2 can normally expose Linux services to Windows through `localhost`, but that behavior depends on the active WSL networking configuration and local firewall/network settings.

For this project, the default development path uses **ADB reverse port forwarding** so the Android app can consistently connect to `127.0.0.1:5555` on the emulator:

```text
Android app 127.0.0.1:5555
        |
        | adb reverse tcp:5555 tcp:5555
        v
Windows localhost:5555
        |
        | WSL2 localhost forwarding / optional portproxy
        v
WSL2 simulator 0.0.0.0:5555
```

This forwarding is only development plumbing. It does not change the project architecture: the application still consumes a TCP telemetry stream produced by the Linux simulator.

## Recommended workflow

1. In WSL2, keep the simulator running:

   ```bash
   ./scripts/run_simulator.sh --tcp-only
   ```

2. Boot the Android Automotive emulator in Android Studio.

3. In **Windows PowerShell** from the repository root, run:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\windows\prepare_android_telemetry.ps1
   ```

   The script first verifies that Windows can reach the WSL2 server on `127.0.0.1:5555`, then configures:

   ```text
   adb reverse tcp:5555 tcp:5555
   ```

4. Run the Android app from Android Studio. Its default endpoint is `127.0.0.1:5555`.

## If Windows localhost cannot reach WSL2

First confirm the server is still running and bound to all interfaces. The simulator prints:

```text
TCP telemetry listening on 0.0.0.0:5555
```

From Windows PowerShell, get the current WSL2 address:

```powershell
wsl hostname -I
```

Then test it:

```powershell
Test-NetConnection <WSL_IP> -Port 5555
```

If the WSL IP works but `127.0.0.1:5555` does not, an elevated PowerShell fallback is:

```powershell
netsh interface portproxy add v4tov4 listenaddress=127.0.0.1 listenport=5555 connectaddress=<WSL_IP> connectport=5555
```

WSL2 NAT addresses can change after WSL restarts, so this is a fallback rather than the preferred permanent configuration. Windows 11 systems can also use WSL mirrored networking, which improves host/WSL localhost behavior.

## Remove the ADB reverse rule

```powershell
adb reverse --remove tcp:5555
```

## Direct endpoint override

The activity still supports endpoint overrides for diagnostics:

```powershell
adb shell am start `
  -n com.example.autotelemetry/.ui.MainActivity `
  --es telemetry_host 10.0.2.2 `
  --ei telemetry_port 5555
```

Use this only after confirming that the chosen address actually reaches the telemetry server from the emulator.
