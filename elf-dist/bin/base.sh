#!/bin/bash
set -euo pipefail

TIMESTAMP=$(date +%s)
ELF_COF_DIR="$ELF_BIN_DIR/../conf"
ELF_PID_DIR="$ELF_BIN_DIR/../pid"
ELF_LOG_DIR="$ELF_BIN_DIR/../logs"
ELF_LIB_DIR="$ELF_BIN_DIR/../lib"
ELF_STORE_DIR="$ELF_BIN_DIR/../store"

if [[ ! -w "$ELF_PID_DIR" ]] ; then
mkdir -p "$ELF_PID_DIR"
fi

if [[ ! -w "$ELF_LOG_DIR" ]] ; then
mkdir -p "$ELF_LOG_DIR"
fi

if [[ ! -w "$ELF_STORE_DIR" ]] ; then
mkdir -p "$ELF_STORE_DIR"
fi

CLASSPATH="$ELF_COF_DIR"
for i in "$ELF_BIN_DIR"/../lib/*
do
CLASSPATH="$i:$CLASSPATH"
done