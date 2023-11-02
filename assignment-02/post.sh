#/usr/bin/bash

curl http://ec2-35-167-216-188.us-west-2.compute.amazonaws.com:8080/server_war/albums \
	-X POST \
	-H "Content-Type: multipart/form-data" \
	-F image=@bollocks.png \
	-F profile='{"artist": "Sex Pistols", "title": "Never Mind the Bollocks", "year": 1983}'
