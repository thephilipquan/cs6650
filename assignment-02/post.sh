#/usr/bin/bash

curl http://localhost:8080/api/albums \
	-X POST \
	-H "Content-Type: multipart/form-data" \
	-F image=@bollocks.png \
	-F profile='{"artist": "Sex Pistols", "title": "Never Mind the Bollocks", "year": 1983}'
