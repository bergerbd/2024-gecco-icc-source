#!/bin/sh

export EVOAL_HOME=$( cd -- "$(dirname $0)/evoal-release" >/dev/null 2>&1 ; pwd -P )

$SHELL $EVOAL_HOME/bin/evoal-search.sh . evoal-configuration/search.ol output -Bcore:search-path=evoal-configuration
