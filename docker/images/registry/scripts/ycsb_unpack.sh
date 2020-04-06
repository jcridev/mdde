#!/bin/sh

# Arguments
YCSB_PACKAGE_DIR=$1
YCSB_BIN_DIR=$2

# Move YCSB to the test destination folder
cd $YCSB_PACKAGE_DIR
ycsb_dist="$(ls  distribution/target/ycsb-*.tar.gz | sort -V | tail -n1)"
if [ ! -f "$ycsb_dist" ]; then
    echo "$ycsb_dist does not exist"
    exit 1
fi
# Make sure the destination folder is empty and exists
if [ -d "$YCSB_BIN_DIR" ]; then rm -Rf $YCSB_BIN_DIR; fi
mkdir -p $YCSB_BIN_DIR
# Unpack YCSB
tar xfvz $ycsb_dist -C $YCSB_BIN_DIR --strip 1
