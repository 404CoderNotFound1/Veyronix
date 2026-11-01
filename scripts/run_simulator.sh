#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT/scripts/build_simulator.sh"
exec "$ROOT/vehicle-simulator/build/vehicle_telemetry_simulator" "$@"
