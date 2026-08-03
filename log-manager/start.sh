#!/usr/bin/env sh
# start.sh — creates reckon-ext-net if absent, then starts the stack
set -e

NETWORK="reckon-ext-net"

if ! docker network inspect "$NETWORK" > /dev/null 2>&1; then
  echo "[start] Network '$NETWORK' not found — creating..."
  docker network create "$NETWORK"
else
  echo "[start] Network '$NETWORK' already exists — skipping create."
fi

docker compose up -d --build "$@"
