# Veyronix: Android Automotive Telemetry & Infotainment Platform

Veyronix is an end-to-end vehicle telemetry and infotainment integration platform. It couples a high-performance Linux C++ vehicle dynamics simulator with a modern Android Automotive OS (AAOS) head-unit application. The platform models real-time in-vehicle data streaming over a dual-layer communications bus—simultaneously broadcasting raw CAN frames across a Linux SocketCAN virtual interface and serving a concurrent TCP telemetry stream consumed by the Android Automotive client.

---

## 1. System Architecture

The system decouples vehicle physics simulation from infotainment visualization through a gateway interface model. This mirrors production automotive topologies where head units consume filtered vehicle telemetry via an IVI (In-Vehicle Infotainment) gateway rather than directly attaching to physical CAN buses.

```text
+--------------------------------------------------------------------+
|                    Veyronix Vehicle Simulator                     |
|                           (Linux C++17)                            |
+---------------------------------+----------------------------------+
                                  |
            +---------------------+---------------------+
            | (Raw CAN Frames)                          | (TCP Stream)
            v                                           v
+-----------------------+                   +------------------------+
| Linux Kernel SocketCAN|                   |  POSIX TCP Server      |
|    Interface (vcan0)  |                   |      (Port 8080)       |
+-----------------------+                   +-----------+------------+
            |                                           |
            v                                           | (ADB Reverse /
     [candump / CAN-bus                                 |  Gateway Bridge)
       Diagnostic Tools]                                v
                                            +------------------------+
                                            |   Android Automotive   |
                                            |      Application       |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            |   TelemetryTcpClient   |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            | TelemetryStreamService |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            |     CanFrameParser     |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            |   VehicleRepository    |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            |     MainViewModel      |
                                            +-----------+------------+
                                                        |
                                                        v
                                            +------------------------+
                                            |    StateFlow / UI      |
                                            | (Infotainment Console) |
                                            +------------------------+
```

### Component Boundaries
* **Physics & Telemetry Simulator (C++17):** Deterministic engine generating vehicle speed, battery state of charge (SoC), ambient thermal data, and discrete system status flags.
* **SocketCAN Publisher:** Emits standardized `struct can_frame` structures directly to the kernel network subsystem (`vcan0`).
* **TCP Gateway Server:** Multi-threaded POSIX socket server delivering live ASCII CAN-formatted frames (`ID#DATA\n`) to connected network clients.
* **Android Infotainment Client (Kotlin / Java):** Android Automotive application structured with Clean Architecture and MVVM patterns, utilizing Kotlin Coroutines and `StateFlow` for deterministic UI updates and automated network reconnection.

---

## 2. Telemetry Protocol Specification

Telemetry frames follow the standard SocketCAN ASCII representation: `ID#DATA\n`, where `ID` is a 3-character hexadecimal CAN arbitration ID and `DATA` is an even-length hexadecimal string representing big-endian byte arrays.

### CAN Arbitration Matrix

| CAN ID | Signal Name | Data Length | Byte Layout | Encoding / Scaling | Valid Range |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `0x100` | Vehicle Speed | 2 Bytes | `[Byte 0: High, Byte 1: Low]` | Big-endian unsigned integer, scale factor 0.1 km/h per LSB | 0.0 km/h to 300.0 km/h |
| `0x101` | Battery SoC | 1 Byte | `[Byte 0: Percentage]` | 8-bit unsigned integer, scale factor 1% per LSB | 0% to 100% |
| `0x102` | Ambient Temperature | 2 Bytes | `[Byte 0: High, Byte 1: Low]` | Big-endian two's complement signed integer, scale factor 0.1 °C per LSB | -50.0 °C to +80.0 °C |
| `0x103` | Vehicle Status Flags | 1 Byte | `[Byte 0: Bitmask]` | Discrete bit fields (see bitmask specification below) | Bitmask `0x00` - `0x07` |

### Status Flag Bitmask (`0x103`)

* **Bit 0 (`0x01`):** Door Open Indicator (`1` = Door Open, `0` = All Doors Closed)
* **Bit 1 (`0x02`):** Traction Battery Charging State (`1` = Charging Active, `0` = Disconnected/Idle)
* **Bit 2 (`0x04`):** Master System Warning (`1` = Active Diagnostic Alert, `0` = Normal)
* **Bits 3–7:** Reserved for future chassis expansion

---

## 3. Repository Structure

```text
Veyronix/
├── .github/
│   └── workflows/
│       ├── android.yml            # Android CI: Unit tests, lint, and Espresso runner
│       └── simulator.yml          # Linux CI: CMake build and CTest test runner
├── app/                           # Android Automotive OS head-unit application
│   ├── src/
│   │   ├── main/                  # Application source (Kotlin & Java protocol core)
│   │   │   ├── java/.../data/     # Network client, frame parsers, and repositories
│   │   │   ├── java/.../domain/   # Business logic, state evaluators, domain models
│   │   │   ├── java/.../ui/       # MVVM activities, view models, and layouts
│   │   │   └── res/               # AAOS UI layouts (standard and 600dp+ automotive screens)
│   │   ├── test/                  # JVM JUnit unit tests (parsers, reducers, viewmodels)
│   │   └── androidTest/           # Instrumented UI & integration tests (Espresso)
│   ├── build.gradle.kts           # Application-level Gradle build configuration
│   └── proguard-rules.pro        # ProGuard release configuration
├── docs/                          # Architectural decision records and design documentation
│   ├── ADR-001-telemetry-boundary.md
│   ├── ARCHITECTURE.md
│   ├── REQUIREMENTS.md
│   ├── TEST_PLAN.md
│   └── WINDOWS_WSL2_NETWORKING.md
├── scripts/                       # Automation scripts for Linux and Windows hosts
│   ├── build_simulator.sh         # Linux CMake compilation script
│   ├── run_simulator.sh           # Linux runtime launch script
│   ├── setup_vcan.sh              # Kernel SocketCAN initialization script
│   └── windows/
│       └── prepare_android_telemetry.ps1 # PowerShell bridge configuration script
├── vehicle-simulator/             # C++17 Vehicle Dynamics and CAN Gateway Simulator
│   ├── include/                   # Header definitions (CanPublisher, TcpServer, etc.)
│   ├── src/                       # Implementation files (main, dynamics engine, sockets)
│   ├── tests/                     # CTest integration test suite
│   └── CMakeLists.txt             # Simulator build configuration
├── build.gradle.kts               # Root Gradle project configuration
├── settings.gradle.kts            # Multi-project Gradle settings
└── LICENSE                        # MIT License
```

---

## 4. Engineering & Architectural Standards

### Android Automotive Layer
* **Clean Architecture:** Strict separation between data access (`TelemetryTcpClient`, `CanFrameParser`), domain entities (`VehicleTelemetry`, `SystemStatusEvaluator`), and presentation layers.
* **Reactive State Streams:** Employs Kotlin Coroutines and `StateFlow` to guarantee thread-safe state distribution across configuration changes without leaking Android lifecycle dependencies.
* **Resilient Socket Client:** Non-blocking TCP transport featuring exponential backoff reconnection logic, frame framing validation, and comprehensive malformed packet rejection.
* **Mixed-Language Interoperability:** Uses `TelemetryProtocol.java` for zero-overhead bit-shifting arithmetic consumed seamlessly by Kotlin domain parsers.

### Vehicle Simulator Layer
* **C++17 Implementation:** Compiled with strict diagnostic compiler flags (`-Wall -Wextra -Wpedantic`).
* **Kernel SocketCAN Integration:** Uses native Linux `AF_CAN` raw sockets with `struct can_frame` validation.
* **Non-Blocking Network Layer:** Multi-client TCP server utilizing POSIX sockets for concurrent telemetry broadcasting.

---

## 5. Development & Deployment Guide

### Prerequisites
* **Host Operating System:** Linux (native or WSL2 on Windows 10/11)
* **Linux Toolchain:** GCC/Clang with C++17 support, CMake 3.16+, `can-utils`, `iproute2`
* **Android Development Toolchain:** JDK 17, Android SDK Platform 35, Android Automotive OS System Image

---

### Step 1: Initialize Linux Virtual CAN (`vcan0`)

On a Linux host (or WSL2 environment with kernel CAN support):

```bash
sudo modprobe vcan
sudo ip link add dev vcan0 type vcan
sudo ip link set up vcan0
```

Verify that the network interface is operational:

```bash
ip link show vcan0
```

---

### Step 2: Build and Execute the C++ Simulator

From the root of the repository:

```bash
# Configure and compile
cmake -S vehicle-simulator -B vehicle-simulator/build -DCMAKE_BUILD_TYPE=Release
cmake --build vehicle-simulator/build -j

# Execute simulator on vcan0 exposing TCP port 8080
./vehicle-simulator/build/vehicle_telemetry_simulator vcan0 8080
```

To observe live raw CAN traffic directly on the bus in a secondary terminal:

```bash
candump vcan0
```

---

### Step 3: Configure Android Automotive Bridge

#### Option A: Running on Android Studio Emulator (Windows Host with WSL2 Simulator)

When running the simulator inside WSL2 and the Android Emulator on Windows, forward port 8080 through ADB to bridge the host and guest networks:

```powershell
# In Windows PowerShell:
adb reverse tcp:8080 tcp:8080
```

Alternatively, run the automated provisioning script:

```powershell
.\scripts\windows\prepare_android_telemetry.ps1
```

#### Option B: Native Linux Development

The emulator connects directly to the host machine gateway IP (`10.0.2.2:8080`).

---

### Step 4: Build and Deploy the Android Automotive Application

Compile and install the debug APK to the running Android Automotive device or emulator:

```bash
./gradlew installDebug
```

Launch the application activity:

```bash
adb shell am start -n com.example.autotelemetry/.ui.MainActivity
```

Monitor telemetry parsing events and connection state in real time:

```bash
adb logcat -s TelemetryTcpClient TelemetryStreamService VehicleRepository
```

---

## 6. Automated Testing & Verification

The project enforces automated test coverage across both the embedded simulator and the Android client.

### Android Unit Tests
Validates frame parsing logic, numeric boundaries, bitmask evaluation, and ViewModel state transitions:

```bash
./gradlew testDebugUnitTest
```

### Android Instrumentation Tests
Executes full-stack Espresso UI and live loopback socket tests on a running Android device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

### Linux C++ Simulator Tests
Runs CTest verification for frame generation and network buffer encoding:

```bash
ctest --test-dir vehicle-simulator/build --output-on-failure
```

---

## 7. Continuous Integration (CI)

GitHub Actions workflows are located in `.github/workflows/`:
* `android.yml`: Compiles the Android project, runs lint analysis, executes JUnit unit tests, and spins up a headless Android API 35 emulator to execute automated Espresso instrumentation tests.
* `simulator.yml`: Compiles the C++17 simulator across Ubuntu targets and executes all CTest test cases.

---

## 8. License

This project is licensed under the terms of the [MIT License](LICENSE).
