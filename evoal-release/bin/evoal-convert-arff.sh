#!/bin/bash

if [ -z ${EVOAL_HOME+x} ]; then
  EVOAL_HOME=$( cd -- "$(dirname $0)/../" >/dev/null 2>&1 ; pwd -P )
fi

source $EVOAL_HOME/bin/paths.env

if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <execution-folder> <input.arff> <specification.ddl> <output.json>"
    exit 1
fi

cd "$1" || exit 1

POSITIONAL_ARGUMENTS=( "$@" )
POSITIONAL_ARGUMENTS=("${POSITIONAL_ARGUMENTS[@]:4}")

declare -a LOCAL_JVM_ARGUMENTS=()

if [ ${EVOAL_VM+x} ]; then
  LOCAL_JVM_ARGUMENTS+=( "${EVOAL_VM[@]}" )
fi

if [ ${EVOAL_DEBUG+x} ]; then
  LOCAL_JVM_ARGUMENTS+=( "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044" )
fi

if [ ${EVOAL_LOGGING+x} ]; then
  POSITIONAL_ARGUMENTS+=( "-Bcore:logging=$EVOAL_LOGGING" )
fi

set -x
java ${LOCAL_JVM_ARGUMENTS[@]} \
     ${EVOAL_JVM_ARGUMENTS[@]} \
     ${POSITIONAL_ARGUMENTS[@]} \
     -Bcore:main=convert-arff-to-json \
     -Barff:input=$2 \
     -Barff:ddl-specification=$3 \
     -Barff:output=$4
