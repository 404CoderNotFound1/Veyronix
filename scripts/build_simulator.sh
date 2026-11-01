#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cmake -S "$ROOT/vehicle-simulator" -B "$ROOT/vehicle-simulator/build" -DCMAKE_BUILD_TYPE=Debug
cmake --build "$ROOT/vehicle-simulator/build" -j
ctest --test-dir "$ROOT/vehicle-simulator/build" --output-on-failure
