#!/usr/bin/env bash

# Check if program already running
if [ -z $(pgrep -f ThetaEngine) ]; then
  bin/theta 2>theta_stderr.log &
else
  echo "ThetaEngine already running under pid: $(pgrep -f ThetaEngine)"
fi
