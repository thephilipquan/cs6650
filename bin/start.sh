#!/usr/bin/bash

. ./source-me.sh

if [ $1 == $SERVER_KEY ]; then
	echo must login to server to start manually
elif [ $1 == $CONSUMER_KEY ]; then
	if [ $# != 2 ]; then
		echo must pass threadCount for consumers
		exit 1
	fi
	for url in "${CONSUMERS[@]}"; do
		ssh -i $PEMKEY ec2-user@$url -f "java -jar consumer.jar $2"
	done
else
	echo "no destination found for $1. pass one of the following (${KEYS[*]})"
	exit 1
fi


