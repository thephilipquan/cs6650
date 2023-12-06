#!/usr/bin/bash

. ./source-me.sh

url=""

if [ $1 == $SERVER_KEY ]; then
	url=$SERVER
elif [ $1 == $CONSUMER_KEY ]; then
	if [ $# != 2 ]
	then
		echo must pass consumer index
		exit 1
	fi
	url=${CONSUMERS[$2]}
else
	echo no destination found for $1
	exit 1
fi

ssh -i $PEMKEY ec2-user@$url
