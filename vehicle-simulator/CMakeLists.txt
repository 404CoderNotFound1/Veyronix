cmake_minimum_required(VERSION 3.16)
project(veyronix_simulator LANGUAGES CXX)

if(NOT CMAKE_SYSTEM_NAME STREQUAL "Linux")
    message(FATAL_ERROR "SocketCAN simulator requires Linux. Use WSL2 or a Linux VM on non-Linux hosts.")
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(simulator_core
    src/CanPublisher.cpp
    src/TcpTelemetryServer.cpp
    src/TelemetryGenerator.cpp
)
target_include_directories(simulator_core PUBLIC include)
target_compile_options(simulator_core PRIVATE -Wall -Wextra -Wpedantic)

add_executable(vehicle_telemetry_simulator src/main.cpp)
target_link_libraries(vehicle_telemetry_simulator PRIVATE simulator_core)

include(CTest)
if(BUILD_TESTING)
    add_executable(simulator_tests tests/simulator_tests.cpp)
    target_link_libraries(simulator_tests PRIVATE simulator_core)
    add_test(NAME simulator_tests COMMAND simulator_tests)
endif()
