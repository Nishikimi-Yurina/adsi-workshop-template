#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

export SAGEMAKER=1
export NEXT_PUBLIC_API_BASE=/codeeditor/default/absports/3000

echo "=== Stopping existing processes ==="
fuser -k 3000/tcp 2>/dev/null || true
fuser -k 3001/tcp 2>/dev/null || true
fuser -k 8080/tcp 2>/dev/null || true
sleep 2

echo "=== Starting Backend (H2, test profile) ==="
cd "$ROOT_DIR/backend"
./gradlew bootRun --args='--spring.profiles.active=test' > /tmp/backend.log 2>&1 &
BACKEND_PID=$!

for i in {1..30}; do
  if grep -q "Started AttendanceApplication" /tmp/backend.log 2>/dev/null; then
    echo "Backend started (${i}s) [PID=$BACKEND_PID]"
    break
  fi
  sleep 1
done

if ! grep -q "Started AttendanceApplication" /tmp/backend.log 2>/dev/null; then
  echo "ERROR: Backend failed to start"
  tail -5 /tmp/backend.log
  exit 1
fi

echo "=== Building Frontend ==="
cd "$ROOT_DIR/frontend"
npx next build > /tmp/frontend-build.log 2>&1
echo "Frontend build complete"

echo "=== Starting Next.js (port 3001) ==="
npx next start -H 127.0.0.1 -p 3001 > /tmp/frontend.log 2>&1 &
NEXT_PID=$!
sleep 3
echo "Next.js started [PID=$NEXT_PID]"

echo "=== Starting SageMaker Proxy (port 3000) ==="
node "$ROOT_DIR/frontend/scripts/sagemaker-proxy.mjs" > /tmp/proxy.log 2>&1 &
PROXY_PID=$!
sleep 1
echo "Proxy started [PID=$PROXY_PID]"

echo ""
echo "=== All services running ==="
echo "  Backend:  http://localhost:8080 [PID=$BACKEND_PID]"
echo "  Next.js:  http://localhost:3001 [PID=$NEXT_PID]"
echo "  Proxy:    http://localhost:3000 [PID=$PROXY_PID]"
echo ""
echo "=== Access URL ==="
echo "  PORTS タブで 3000 の地球儀ボタン → URL の ports を absports に置換"
echo ""
echo "  Stop: bash $SCRIPT_DIR/dev-sagemaker-stop.sh"
