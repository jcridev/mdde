#!/bin/sh

# Check if JDK is installed
java_version=$(java -version 2>&1 >/dev/null | egrep "\S+\s+version" | awk '{print $3}' | tr -d '"')
min_jdk_version=11.0.0
if [ "$(printf '%s\n' "$min_jdk_version" "$java_version" | sort -V | head -n1)" != "$min_jdk_version" ]; then 
    echo "JDK version $min_jdk_version or newer is required"
    exit 1
fi

# Check if Maven is installed
mvn -v
if [ "$?" -ne 0 ]; then
    echo "Maven is not installed"
    exit 1
fi

TEST_DIR="$(pwd)"
echo $TEST_DIR

# Build the Registry
cd ../registry/mdde-registry
REGISTRY_ROOT=$PWD

mvn -Dmaven.test.skip=true clean package
mvn clean install --non-recursive
# Install common shared lib
cd $REGISTRY_ROOT/shared/mdde-registry-shared
mvn clean install -DskipTests
# Install TCP specific shared lib
cd $REGISTRY_ROOT/shared/mdde-registry-tcp-shared
mvn clean install -DskipTests

# Get YCSB for MDDE
cd $TEST_DIR
ycsb_git=ycsb_git
if [ ! -d "$ycsb_git" ] ; then
    git clone --single-branch --branch redis-mdde-client https://github.com/akharitonov/YCSB.git $ycsb_git
else
    cd $ycsb_git
    git pull https://github.com/akharitonov/YCSB.git
    cd ..
fi

# Build YCSB
cd $ycsb_git
mvn clean package -DskipTests

# Move YCSB to the test destination folder
ycsb_bin=../ycsb
ycsb_dist="$(ls  distribution/target/ycsb-*.tar.gz | sort -V | tail -n1)"
if [ ! -f "$ycsb_dist" ]; then
    echo "$ycsb_dist does not exist"
    exit 1
fi
if [ -d "$ycsb_bin" ]; then rm -Rf $ycsb_bin; fi
mkdir -p $ycsb_bin
tar xfvz $ycsb_dist -C $ycsb_bin --strip 1
