#!/bin/bash

offline=false
working_dir=$2

if [ $# > 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

echo "Installing Java 8 in /opt/java"
cd /opt
mkdir java
cd java

if [ $offline = true ]
then
    cp $working_dir/java/jdk-8u92-linux-x64.tar.gz .
else
    curl -L -O -H "Cookie: oraclelicense=accept-securebackup-cookie" -k "http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.tar.gz"
fi

if ! [ -f jdk-8u92-linux-x64.tar.gz ]
then
    echo "Working in offline mode and file not found.. exiting"
    exit 1
fi

tar -xvf jdk-8u92-linux-x64.tar.gz
rm -f jdk-8u92-linux-x64.tar.gz
echo "Creating symbolic link called 'current' to simplify upgrades"
ln -s jdk1.8.0_92 current