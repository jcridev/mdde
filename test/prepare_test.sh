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

# Get YCSB for MDDE
ycsb_git=ycsb_git
if [ ! -d "$ycsb_git" ] ; then
    git clone --single-branch --branch redis-mdde-client https://github.com/jcridev/YCSB.git $ycsb_git
else
    cd $ycsb_git
    git pull https://github.com/jcridev/YCSB.git
    cd ..
fi

# Build YCSB
cd $ycsb_git
mvn clean package -DskipTests

# Move YCSB to the test destination folder
ycsb_bin=../ycsb
ycsb_dist="$(ls  distribution/target/ycsb-*.tar.gz | sort -V | tail -n1)"
mkdir -p $ycsb_bin
tar xfvz $ycsb_dist -C $ycsb_bin --strip 1
