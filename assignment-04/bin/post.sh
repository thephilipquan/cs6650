#!/usr/bin/bash

. ./source-me.sh

curl $SERVER:8080/$SERVER_ARTIFACT/albums \
	-X POST \
	-H "Content-Type: multipart/form-data" \
	-F image=@../../bollocks.png \
	-F profile='{"artist": "Sex Pistols", "title": "Never Mind the Bollocks", "year": 1983}'

curl $SERVER:8080/$SERVER_ARTIFACT/reviews/like/1 \
	-X POST
curl $SERVER:8080/$SERVER_ARTIFACT/reviews/dislike/1 \
	-X POST
curl $SERVER:8080/$SERVER_ARTIFACT/reviews/dislike/1 \
	-X POST
