# Software Requirements

## Functional requirements

| ID | Requirement | Demonstration |
|---|---|---|
| FR-01 | Display vehicle speed from simulated CAN telemetry. | `100#` frame updates speed card. |
| FR-02 | Display battery percentage from simulated CAN telemetry. | `101#` frame updates battery. |
| FR-03 | Display ambient temperature from simulated CAN telemetry. | `102#` frame updates ambient temperature. |
| FR-04 | Decode vehicle status flags for door, charging and warning state. | `103#` bitfield updates flags/notification. |
| FR-05 | Provide mock media previous/play-pause/next controls. | Media card updates track/playback state. |
| FR-06 | Provide simulated climate set-point and fan-mode controls. | Climate card changes 16–30°C in 0.5°C steps. |
| FR-07 | Report telemetry connection and parsing failures. | Error banner and reconnect button. |
| FR-08 | Produce Linux SocketCAN frames on a virtual interface. | Simulator writes `can_frame` values to `vcan0`. |
| FR-09 | Mirror CAN messages over TCP for the Android prototype. | Simulator listens on TCP port 5555 and sends `ID#DATA` lines. |

## Non-functional requirements

- NFR-01: Separate UI, domain, repository, service, network and parser responsibilities.
- NFR-02: Parser input is validated before data reaches UI state.
- NFR-03: UI supports narrow and `sw600dp` two-column layouts for automotive displays.
- NFR-04: Core parsing and state logic is covered by local JUnit tests.
- NFR-05: Critical UI workflow and error behavior is covered by Espresso instrumentation tests.
- NFR-06: GitHub Actions runs Android build/unit/instrumentation validation and C++ build/tests.
