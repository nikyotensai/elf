#!/bin/bash

. "$ELF_BIN_DIR/base.sh"


if [[ "$JAVA_HOME" != "" ]];then
    JAVA_HOME="$JAVA_HOME"
else
    echo "Please set JAVA_HOME env before run this script"
    exit 1
fi

