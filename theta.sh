#!/usr/bin/env bash

# Check if program already running
if [ -z $(pgrep -f ThetaEngine) ]; then
  java -classpath "/opt/theta/lib/*" theta.ThetaEngine > /opt/theta/theta.log 2>&1
else
  echo "ThetaEngine already running under pid: $(pgrep -f ThetaEngine)"
fi
