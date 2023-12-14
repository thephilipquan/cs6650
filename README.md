# cs6650

To run any service (client/server) for any of the assignments. You need to create a `run.conf` file in your resources folder with the following attributes.

```
run.groupCount = <number>
run.groupThreadCount = <number>
run.delayInSeconds = <number>
run.hostUrl = http://<ip-address>:8080/<artifact-name>
run.imagePath = absolute/path/to/image.png
run.outPrefix = report-file-prefix
```
