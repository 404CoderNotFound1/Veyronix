# Test Plan

| Requirement | Test level | Test case |
|---|---|---|
| FR-01–FR-04 | JUnit | `CanFrameParserTest` validates speed, battery, signed temperature, bitfield and malformed values. |
| FR-01–FR-04 | JUnit | `MainViewModelTest` verifies repository telemetry transitions into UI state/notifications. |
| FR-01–FR-04 | JUnit | `VehicleTelemetryReducerTest` verifies update-to-model mapping without overwriting unrelated fields. |
| FR-04/FR-07 | JUnit | `SystemStatusEvaluatorTest` and `MainViewModelTest` verify warning priority, connection-error UI state and reconnect delegation. |
| FR-06 | JUnit | `ClimateControllerTest` verifies 0.5°C steps and 16–30°C clamping. |
| FR-01/02/06 | Espresso | Local TCP fixture sends frames; UI shows speed/battery; climate `+` changes set point. |
| FR-07 | Espresso | Local TCP fixture sends a malformed line; error banner becomes visible. |
| FR-08/09 | CTest | `simulator_tests` verifies generated IDs, payload lengths and text encoding. |
| NFR-06 | CI | Android workflow builds, lints, runs JUnit, then executes Espresso on an emulator; simulator workflow builds/tests C++. |

## TDD examples used in this project

The parser validation, climate boundary rules and ViewModel state behavior are structured as deterministic units so they can be implemented test-first. A practical commit sequence is: add failing test, implement minimum rule, refactor, then commit both.
