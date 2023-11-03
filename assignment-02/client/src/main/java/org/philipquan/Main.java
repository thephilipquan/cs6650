package org.philipquan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    public static void main(final String[] args) {
        if (args.length != 5) {
            throw new RuntimeException("Please provide 5 arguments to run the program. (threadCount, groupCount, delayInSeconds, hostUrl, imagePath)");
        }

        final int threadCount = Integer.parseInt(args[0]);
        final int groupCount = Integer.parseInt(args[1]);
        final int delayInSeconds = Integer.parseInt(args[2]);
        final String hostUrl = args[3];
        final String imagePath = args[4];

        String image = readFileAsString(imagePath);
        ClientApp client = new ClientApp(threadCount, groupCount, delayInSeconds, hostUrl, image, Collections.synchronizedList(new ArrayList<RequestStatistic>()));
        if (!client.hostUrlExists()) {
            throw new RuntimeException("Initial connection to host url: " + hostUrl + " failed.");
        }
        System.out.println("Initial connection to host url: " + hostUrl + " success");
        System.out.println("Initial run...");
        client.initialRun();
        Timer timer = new Timer();
        timer.start();
        client.groupRun();
        timer.stop();
        client.reportStatistics(timer);
    }

    private static String readFileAsString(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}