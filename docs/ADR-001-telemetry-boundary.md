# ADR-001: Use SocketCAN for vehicle-side simulation and TCP for the app boundary

**Status:** Accepted

## Context

A personal Android Automotive project needs to demonstrate CAN concepts without requiring OEM hardware, privileged Vehicle HAL access, or a custom AAOS system image. Linux provides virtual SocketCAN interfaces, while the Android emulator can consume a normal TCP stream. On Windows + WSL2, emulator-host and host-WSL networking are separate boundaries, so development should not assume that `10.0.2.2` automatically reaches a WSL2 process.

## Decision

The C++ process emits every mock frame to `vcan0` and mirrors the same frame to a TCP client. Android consumes TCP and applies the same CAN parsing rules it would apply to a gateway-delivered CAN payload. For Windows + WSL2 development, `adb reverse` forwards emulator `127.0.0.1:5555` to Windows `localhost:5555`, which WSL2 normally forwards to the Linux service; a temporary Windows port proxy is documented as a fallback.

## Consequences

- CAN behavior is observable with normal Linux tools such as `candump`.
- Android development remains unprivileged and reproducible.
- The TCP link models an in-vehicle Ethernet/gateway boundary rather than pretending the Android app has direct CAN access.
- This prototype is not a production VHAL implementation and does not claim OEM integration.
