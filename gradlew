#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.13"
GRADLE_SHA256="20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78"
BASE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/portfolio-bootstrap"
INSTALL_DIR="$BASE_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$BASE_DIR/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
  mkdir -p "$BASE_DIR"
  echo "Gradle $GRADLE_VERSION is not cached; downloading the official distribution..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "$URL"
  else
    echo "Install curl or wget, or install Gradle $GRADLE_VERSION manually." >&2
    exit 1
  fi

  if command -v sha256sum >/dev/null 2>&1; then
    printf '%s  %s\n' "$GRADLE_SHA256" "$ZIP_FILE" | sha256sum -c -
  elif command -v shasum >/dev/null 2>&1; then
    actual="$(shasum -a 256 "$ZIP_FILE" | awk '{print $1}')"
    [ "$actual" = "$GRADLE_SHA256" ] || { echo "Gradle checksum mismatch" >&2; exit 1; }
  fi

  command -v unzip >/dev/null 2>&1 || { echo "Install unzip and rerun." >&2; exit 1; }
  rm -rf "$INSTALL_DIR"
  unzip -q "$ZIP_FILE" -d "$BASE_DIR"
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
