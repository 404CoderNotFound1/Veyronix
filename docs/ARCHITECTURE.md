# Architecture

## Overview

```text
Linux C++ simulator
  TelemetryGenerator
       |----> CanPublisher ----> SocketCAN vcan0 ----> candump / CAN tooling
       |
       +----> TcpTelemetryServer :5555
                              |
                         Android emulator
                              |
                     TelemetryTcpClient
                              |
                    TelemetryStreamService
                              |
                    VehicleRepositoryImpl
                              |
                        MainViewModel
                              |
                         MainActivity
```

## Android layers

- **UI:** `MainActivity`, `MainViewModel`, `InfotainmentUiState`. The activity renders immutable state and forwards user actions.
- **Domain:** telemetry models, repository interface and deterministic climate/status business rules.
- **Data:** `VehicleRepositoryImpl`, reducer, parser, TCP client and stream service.
- **Protocol boundary:** `CanFrameParser` is the only component that understands `ID#DATA` wire messages.

This is intentionally manual dependency construction instead of a DI framework. For a small personal project it keeps the architecture visible in interviews without adding Hilt/Dagger boilerplate.

## CAN protocol

| CAN ID | Bytes | Encoding |
|---|---:|---|
| `0x100` | 2 | unsigned big-endian speed in 0.1 km/h |
| `0x101` | 1 | battery percent, 0–100 |
| `0x102` | 2 | signed big-endian ambient temperature in 0.1°C |
| `0x103` | 1 | bit 0 door open, bit 1 charging, bit 2 warning |

The TCP transport intentionally sends the same frame representation as text (`100#04D2`) so the Android side can be developed without Linux SocketCAN APIs or privileged AAOS vehicle services.


## Windows/WSL2 development transport

The logical architecture remains simulator TCP server → Android TCP client. When the Linux simulator runs in WSL2 and Android Studio runs on Windows, the recommended local development plumbing is:

```text
Android 127.0.0.1:5555
    -> adb reverse
Windows localhost:5555
    -> WSL2 localhost forwarding (or a temporary portproxy fallback)
WSL2 simulator :5555
```

`adb reverse` is not presented as an in-vehicle production mechanism. It is only a reproducible workstation bridge that avoids pretending the emulator can directly discover the WSL2 VM.
