#!/bin/sh
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Gradle not found on PATH. Please install Gradle 8.4+ or run via GitHub Actions." >&2
    exit 1
fi
