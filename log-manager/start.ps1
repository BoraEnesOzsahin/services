# start.ps1 — creates reckon-ext-net if absent, then starts the stack
$Network = "reckon-ext-net"

$exists = docker network inspect $Network 2>$null
if (-not $exists) {
    Write-Host "[start] Network '$Network' not found — creating..."
    docker network create $Network
} else {
    Write-Host "[start] Network '$Network' already exists — skipping create."
}

docker compose up -d --build @args
