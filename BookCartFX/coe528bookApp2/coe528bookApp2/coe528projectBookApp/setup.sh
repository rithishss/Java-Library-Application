#!/bin/sh
# Downloads the JavaFX SDK the build needs into lib/.
#
#     cd BookCartFX/coe528bookApp2/coe528bookApp2/coe528projectBookApp
#     ./setup.sh
#
# The SDK is a ~50 MB platform-specific download, so it is not committed. Every
# platform unpacks to the same lib/javafx-sdk-<version>/ directory, which is
# what nbproject/project.properties points at.
#
# Windows users without a shell: download the matching bundle by hand from
# https://gluonhq.com/products/javafx/ and unzip it into lib/.

set -e

VERSION=25.0.1
TARGET="lib/javafx-sdk-${VERSION}"

cd "$(dirname "$0")"

if [ -f "${TARGET}/lib/javafx.controls.jar" ]; then
    echo "JavaFX ${VERSION} is already in ${TARGET}, nothing to do."
    exit 0
fi

case "$(uname -s)" in
    Darwin)  OS=osx ;;
    Linux)   OS=linux ;;
    MINGW*|MSYS*|CYGWIN*) OS=windows ;;
    *) echo "Unrecognised system $(uname -s). Download the SDK by hand from https://gluonhq.com/products/javafx/ and unzip it into lib/." >&2
       exit 1 ;;
esac

case "$(uname -m)" in
    arm64|aarch64) ARCH=aarch64 ;;
    x86_64|amd64)  ARCH=x64 ;;
    *) echo "Unrecognised architecture $(uname -m)." >&2; exit 1 ;;
esac

# Gluon publishes macOS Intel as osx-x64, but Linux and Windows ARM builds are
# not offered, so fall back to x64 there.
if [ "$OS" != "osx" ] && [ "$ARCH" = "aarch64" ]; then
    ARCH=x64
fi

ARCHIVE="openjfx-${VERSION}_${OS}-${ARCH}_bin-sdk.zip"
URL="https://download2.gluonhq.com/openjfx/${VERSION}/${ARCHIVE}"

echo "Downloading ${ARCHIVE}"
mkdir -p lib
curl -fL --progress-bar -o "lib/${ARCHIVE}" "$URL"

echo "Unpacking into lib/"
unzip -q -o "lib/${ARCHIVE}" -d lib
rm -f "lib/${ARCHIVE}"

if [ ! -f "${TARGET}/lib/javafx.controls.jar" ]; then
    echo "Unpacked, but ${TARGET}/lib/javafx.controls.jar is missing. Check lib/ by hand." >&2
    exit 1
fi

echo "JavaFX ${VERSION} ready in ${TARGET}."
echo "Now open this folder in NetBeans and press F6, or run: ant run"
