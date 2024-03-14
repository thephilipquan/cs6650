# cs6650

## summary

This repo consists of three java projects.

1. Server - Tomcat, AWS, PostgreSQL - A backend web application that exposes functionality for adding, requesting info, and reacting to a music album.
2. Consumer - RabbitMQ - A RabbitMQ consumer that eventually updates the database with time-insensitive posts.
3. Client - A mock project to spam the server with 100,000+ requests to simulate user load in real time.

## tech stack

* [Apache Tomcat](https://tomcat.apache.org/)
* [RabbitMQ](https://www.rabbitmq.com/)
* [AWS EC2](https://aws.amazon.com/ec2/)
* [AWS RDS](https://aws.amazon.com/rds/)

## how to run

Each project relies on a properties file located in the `<root-project>/src/main/resources/` directory of the project with the following attributes...

### client

```
run.groupCount = <number>
run.groupThreadCount = <number>
run.delayInSeconds = <number>
run.hostUrl = http://<ip-address>:8080/<artifact-name>
run.imagePath = absolute/path/to/image.png
run.outPrefix = report-file-prefix
```

### server

```
db.url = <string>
db.user = <string>
db.password = <string>

mq.queueName = <string>
mq.url = <string>
mq.user = <string>
mq.password = <string>
```

### consumer

```
server.properties
db.driver = <string>
db.url = <string>
db.user = <string>
db.password = <string>

mq.url = <string>
mq.username = <string>
mq.password = <string>
```

