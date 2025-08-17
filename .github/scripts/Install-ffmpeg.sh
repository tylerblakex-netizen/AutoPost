#!/bin/bash
set -e
sudo apt update
sudo apt install -y ffmpeg
ffmpeg -version  # Explained: Installs and verifies FFmpeg; used in workflow steps.
