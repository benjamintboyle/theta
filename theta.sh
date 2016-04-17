#!/usr/bin/env bash

# Check if program already running
if [ -z $(pgrep -f ThetaEngine) ]; then
  java -classpath "/opt/theta/lib/*" theta.ThetaEngine 2>/opt/theta/theta_stderr.log
else
  echo "ThetaEngine already running under pid: $(pgrep -f ThetaEngine)"
fi
