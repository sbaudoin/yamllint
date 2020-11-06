#!/bin/bash -e

SCRIPT_DIR=$(dirname $0)
TEMP_DIR=$(mktemp -d)
tar -xzf $SCRIPT_DIR/../target/yamllint-*-distrib.tar.gz -C $TEMP_DIR
$TEMP_DIR/yamllint/bin/yamllint -f parsable test.yml
