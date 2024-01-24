#!/usr/bin/bash

. ./source-me.sh

if [ $1 == $SERVER_KEY ]; then
	scp -i $PEMKEY \
		../server/out/artifacts/$SERVER_ARTIFACT/$SERVER_ARTIFACT.war \
		ec2-user@$SERVER:/usr/share/tomcat/webapps/
elif [ $1 == $CONSUMER_KEY ]; then
	for url in ${CONSUMERS[@]}; do
		scp -i $PEMKEY \
			../consumer/out/artifacts/$CONSUMER_ARTIFACT\_jar/$CONSUMER_ARTIFACT.jar \
			ec2-user@$url:/home/ec2-user/
	done
else
	echo "no destination found for $1. pass one of the following ("${KEYS[*]}")"
	exit 1
fi


