#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MIN_JAVA_MAJOR=17

java_major() {
  local java_bin="$1"
  "$java_bin" -version 2>&1 |
    awk -F'"' '/version/ {print $2; exit}' |
    cut -d. -f1
}

is_usable_java() {
  local java_bin="$1"
  [[ -n "$java_bin" && -x "$java_bin" ]] || return 1
  local major
  major="$(java_major "$java_bin" || true)"
  [[ "$major" =~ ^[0-9]+$ ]] && (( major >= MIN_JAVA_MAJOR ))
}

find_java_home() {
  if [[ -n "${JAVA_HOME:-}" ]] && is_usable_java "$JAVA_HOME/bin/java"; then
    printf '%s\n' "${JAVA_HOME%/}"
    return 0
  fi

  if command -v java >/dev/null 2>&1; then
    local home
    home="$(java -XshowSettings:properties -version 2>&1 |
      awk -F'= ' '/java.home =/ {print $2; exit}')"
    if is_usable_java "$home/bin/java"; then
      printf '%s\n' "$home"
      return 0
    fi
  fi

  return 1
}

download_jdk() {
  local os arch api_os api_arch
  os="$(uname -s)"
  arch="$(uname -m)"

  case "$os" in
    Linux) api_os=linux ;;
    Darwin) api_os=mac ;;
    *)
      echo "Sistema operativo no soportado: $os" >&2
      return 1
      ;;
  esac

  case "$arch" in
    x86_64|amd64) api_arch=x64 ;;
    aarch64|arm64) api_arch=aarch64 ;;
    *)
      echo "Arquitectura no soportada: $arch" >&2
      return 1
      ;;
  esac

  local data_home="${XDG_DATA_HOME:-$HOME/.local/share}"
  local install_dir="$data_home/three-microservices-eureka"
  local jdk_dir="$install_dir/jdk-17"

  if is_usable_java "$jdk_dir/bin/java"; then
    printf '%s\n' "$jdk_dir"
    return 0
  fi

  mkdir -p "$install_dir"

  local tmp archive url
  tmp="$(mktemp -d)"
  archive="$tmp/jdk17.tar.gz"
  url="https://api.adoptium.net/v3/binary/latest/17/ga/${api_os}/${api_arch}/jdk/hotspot/normal/eclipse"

  echo "Java 17 no encontrado. Descargando Temurin 17 ($api_os/$api_arch)..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o "$archive"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$url" -O "$archive"
  else
    echo "Se necesita curl o wget para descargar Java 17." >&2
    rm -rf "$tmp"
    return 1
  fi

  tar -xzf "$archive" -C "$tmp"
  local extracted
  extracted="$(find "$tmp" -maxdepth 1 -type d -name 'jdk-17*' | head -n 1)"
  if [[ -z "$extracted" ]]; then
    echo "No se pudo extraer el JDK descargado." >&2
    rm -rf "$tmp"
    return 1
  fi

  rm -rf "$jdk_dir"
  mv "$extracted" "$jdk_dir"
  rm -rf "$tmp"
  printf '%s\n' "$jdk_dir"
}

JAVA_HOME_DETECTED="$(find_java_home || true)"
if [[ -z "$JAVA_HOME_DETECTED" ]]; then
  JAVA_HOME_DETECTED="$(download_jdk)"
fi

export JAVA_HOME="$JAVA_HOME_DETECTED"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java: $("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
echo "JAVA_HOME=$JAVA_HOME"

if [[ $# -eq 0 ]]; then
  set -- test
fi

cd "$ROOT_DIR"
exec "$ROOT_DIR/mvnw" "$@"
