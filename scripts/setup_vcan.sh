#!/usr/bin/env bash
set -euo pipefail

INTERFACE="${1:-vcan0}"

if [[ "$(uname -s)" != "Linux" ]]; then
  echo "SocketCAN requires Linux. Use Linux or WSL2."
  exit 1
fi

sudo modprobe vcan
if ! ip link show "$INTERFACE" >/dev/null 2>&1; then
  sudo ip link add dev "$INTERFACE" type vcan
fi
sudo ip link set up "$INTERFACE"
ip -details link show "$INTERFACE"
echo "Virtual CAN interface $INTERFACE is ready."
