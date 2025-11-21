#!/bin/bash

echo "Starting Clipers application..."

# Find the JAR file
JAR_FILE=$(find /home/site/wwwroot -name "clipers-*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in /home/site/wwwroot"
    echo "Looking for JAR in target directory..."
    JAR_FILE=$(find /home/site/wwwroot/target -name "clipers-*.jar" -type f | head -n 1)
fi

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: Still no JAR file found. Listing directory contents:"
    ls -la /home/site/wwwroot
    exit 1
fi

echo "Found JAR: $JAR_FILE"
echo "Starting application..."

java -jar "$JAR_FILE"
