#!/bin/sh
set -e
# Elf Agent for Java installation script
if [ -f /elf/agent/pid/elf-agent.pid ] ;then
  cd /elf/agent/bin/
  ./elf-agent.sh  stop
fi

cd /elf/agent/bin/

./elf-agent.sh -p 1 -j $JAVA_HOME start

