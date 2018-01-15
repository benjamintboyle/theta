#!/usr/bin/env bash

pkill -f ThetaEngine

sleep 1

# Archive stderr log if exists and has non-zero size
if [ -s theta_stderr.log ]; then
  STDERR_ARCHIVE="theta_$(date --iso-8601=seconds).log"
  mv theta.log $STDERR_ARCHIVE
fi
