#!/bin/bash
set -euo pipefail

ELF_BIN="${BASH_SOURCE-$0}"
ELF_BIN="$(dirname "$ELF_BIN")"
ELF_BIN_DIR="$(cd "$ELF_BIN"; pwd)"
ELF_MAIN="com.github.nikyotensai.elf.indpendent.agent.Main"


ARTHAS_INPUT_RC_DIR=$HOME"/.arthas/conf"
ARTHAS_INPUT_RC_PATH=$ARTHAS_INPUT_RC_DIR"/inputrc";

. "$ELF_BIN_DIR/base.sh"
JAVA_HOME="/tmp/elf/java"
JAVA_OPTS=""

for CMD in "$@";do true; done

APP_PID=""
LOCAL_IP=""

while getopts p:i:j:c:h opt;do
    case $opt in
        p) APP_PID=$OPTARG;;
        i) LOCAL_IP=$OPTARG;;
        j) JAVA_HOME=$OPTARG;;
        h|*) echo "-p    通过-p指定应用进程pid"
           echo "-i    通过-i参数指定本机ip"
           echo "-j    通过-j指定java home"
           echo "-h    通过-h查看命令帮助"
           exit 0
    esac
done

if [[ "$JAVA_HOME" != "" ]];then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java;
fi

if [[ ! -w "$ARTHAS_INPUT_RC_DIR" ]] ; then
  mkdir -p "$ARTHAS_INPUT_RC_DIR"
fi

if [ ! -f "$ARTHAS_INPUT_RC_PATH" ];then
  cp "$ELF_BIN_DIR"/inputrc "$ARTHAS_INPUT_RC_PATH"
fi

if [[ -n $LOCAL_IP ]]; then
    JAVA_OPTS="$JAVA_OPTS -Delf.local.host=$LOCAL_IP"
fi

if [[ -n $APP_PID ]]; then
    JAVA_OPTS="$JAVA_OPTS -Delf.user.pid=$APP_PID"
fi

JAVA_OPTS="$JAVA_OPTS -Delf.log.dir=$ELF_LOG_DIR -Xmx80m -Xmn50m -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+UseCodeCacheFlushing -Xloggc:${ELF_LOG_DIR}/elf-gc-${TIMESTAMP}.log -XX:+PrintGC -XX:+PrintGCDetails -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${ELF_LOG_DIR}"
ELF_PID_FILE="$ELF_PID_DIR/elf-agent.pid"
ELF_DAEMON_OUT="$ELF_LOG_DIR/elf-agent.out"

resetEvn(){
    local JAVA_VERSION=""
    local IFS=$'\n'
    local tempClassPath=""
    local lines=$("${JAVA_HOME}"/bin/java -version 2>&1 | tr '\r' '\n')
    for line in $lines; do
      if [[ (-z $JAVA_VERSION) && ($line = *"version"*) ]]
      then
        local ver=$(echo $line | sed -e 's/.*version "\(.*\)"\(.*\)/\1/; 1q')
        # on macOS, sed doesn't support '?'
        if [[ $ver = "1."* ]]
        then
          JAVA_VERSION=$(echo $ver | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
        else
          JAVA_VERSION=$(echo $ver | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
        fi
      fi
    done

    # when java version less than 9, we can use tools.jar to confirm java home.
    # when java version greater than 9, there is no tools.jar.
    if [[ "$JAVA_VERSION" -lt 9 ]];then
      # possible java homes
      javaHomes=("${JAVA_HOME%%/}" "${JAVA_HOME%%/}/.." "${JAVA_HOME%%/}/../..")
      for javaHome in ${javaHomes[@]}
      do
          toolsJar="$javaHome/lib/tools.jar"
          saJdiJar="$JAVA_HOME/lib/sa-jdi.jar"
          if [ -f $toolsJar ] && [ -f $saJdiJar ]; then
            tempClassPath="$toolsJar:$saJdiJar"
          fi
      done

      if [ -z $tempClassPath ]; then
          echo "tools.jar and sa-jdi.jar was not found, so elf agent could not be launched!"
          exit 0;
      else
        CLASSPATH="$CLASSPATH:$tempClassPath"
      fi
    else
        JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/jdk.internal.perf=ALL-UNNAMED"
    fi

    echo "JAVA_HOME: $JAVA_HOME"
}

start(){
    echo "Start elf agent ..."

    resetEvn

    nohup "$JAVA" -cp "$CLASSPATH" ${JAVA_OPTS} ${ELF_MAIN} > "$ELF_DAEMON_OUT" 2>&1 < /dev/null &
    if [[ $? -eq 0 ]]
    then
      /bin/echo -n $! > "$ELF_PID_FILE"
      if [[ $? -eq 0 ]];
      then
        sleep 1
        echo STARTED
      else
        echo FAILED TO WRITE PID
        exit 1
      fi
    else
      echo SERVER DID NOT START
      exit 1
    fi
}
stop(){
    echo "Stopping elf agent ... "
    if [[ ! -f "$ELF_PID_FILE" ]]
    then
      echo "no elf agent to stop (could not find file $ELF_PID_FILE)"
    else
      kill $(cat "$ELF_PID_FILE")
      rm "$ELF_PID_FILE"
      echo "STOPPED"
    fi
}

case ${CMD} in
start)
    start
    ;;
stop)
    stop
    exit 0
    ;;
restart)
    stop
    start
    ;;
*)
    echo "Usage: $0 {start|restart|stop}" >&2
esac

