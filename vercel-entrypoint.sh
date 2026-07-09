#!/bin/sh
set -eu

echo "[startup] iniciando HOK Draft Coach"
echo "[startup] PORT=${PORT:-nao-definida}"

JAVA_BIN="/opt/java/openjdk/bin/java"
APP_JAR="/app/app.jar"

if [ ! -x "$JAVA_BIN" ]; then
  echo "[startup] ERRO: Java nao encontrado em $JAVA_BIN" >&2
  ls -la /opt/java/openjdk/bin >&2 || true
  exit 127
fi

if [ ! -f "$APP_JAR" ]; then
  echo "[startup] ERRO: JAR nao encontrado em $APP_JAR" >&2
  ls -la /app >&2 || true
  exit 1
fi

"$JAVA_BIN" -version
exec "$JAVA_BIN" -XX:MaxRAMPercentage=75.0 -jar "$APP_JAR"
