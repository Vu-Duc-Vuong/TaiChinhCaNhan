#!/bin/sh

MAVEN_VERSION="3.9.9"
WRAPPER_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.mvn/wrapper" && pwd)"
MAVEN_CACHE="${TMPDIR:-/tmp}/maven-wrapper"
MAVEN_HOME="$MAVEN_CACHE/apache-maven-$MAVEN_VERSION"
MAVEN_ZIP="${TMPDIR:-/tmp}/apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
    echo "Maven $MAVEN_VERSION was not found. Downloading it..."
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$MAVEN_URL" -o "$MAVEN_ZIP"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$MAVEN_URL" -O "$MAVEN_ZIP"
    else
        echo "curl or wget is required to download Maven."
        exit 1
    fi

    mkdir -p "$MAVEN_CACHE"
    unzip -q -o "$MAVEN_ZIP" -d "$MAVEN_CACHE" || exit 1
fi

exec "$MAVEN_HOME/bin/mvn" "$@"