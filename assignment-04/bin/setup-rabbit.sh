#!/usr/bin/bash

. ./source-me.sh

scp -i $PEMKEY ../resources/rabbitmq.repo ec2-user@$RABBIT:/home/ec2-user

