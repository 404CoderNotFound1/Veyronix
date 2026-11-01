# System Verification and Demonstration Walkthrough

Use this sequence to verify the complete telemetry pipeline end-to-end:

1. **Verify Architecture:** Reference `docs/ARCHITECTURE.md` to identify the distinct SocketCAN and TCP boundaries.
2. **Observe CAN Bus Traffic:** Run `candump vcan0` in a dedicated terminal.
3. **Start the Simulator:** Run `./scripts/run_simulator.sh`; observe consistent `100`–`103` arbitration IDs across terminals.
4. **Initialize AAOS Emulator:** Launch the Android Automotive client and verify connection state updates to **Connected**.
5. **Monitor Live Telemetry:** Verify continuous updates across vehicle speed, battery state-of-charge, and ambient thermal sensors.
6. **Validate Vehicle State:** Monitor generated door, charging, and diagnostic alert flags in real time.
7. **Test Local Infotainment Controls:** Validate media controls and climate adjustment states.
8. **Execute Automated Verification:** Run `./gradlew testDebugUnitTest` and `./gradlew connectedDebugAndroidTest`, verifying CI parity.

## Operational Summary

The C++ simulator acts as an in-vehicle telemetry gateway. It emits raw frames onto Linux SocketCAN (`vcan0`) for standard automotive diagnostic utilities while mirroring telemetry frames over TCP to represent an automotive Ethernet gateway boundary. The Android Automotive client treats TCP strictly as transport: incoming frames undergo strict protocol parsing, the repository reduces delta updates into application state, and the ViewModel exposes immutable reactive UI state via StateFlow.
