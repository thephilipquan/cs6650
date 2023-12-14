#!/usr/bin/bash

. ./source-me.sh

curl "http://$SERVER:8080/$SERVER_ARTIFACT/albums?albumId=$1"

