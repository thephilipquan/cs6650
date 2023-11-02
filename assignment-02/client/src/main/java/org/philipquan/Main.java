package org.philipquan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;

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

        String image = null;
        try {
            image = FileUtils.readFileToString(new File(imagePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClientApp client = new ClientApp(threadCount, groupCount, hostUrl, image, Collections.synchronizedList(new ArrayList<MethodStatistic>()));
        if (!client.hostUrlExists()) {
            throw new RuntimeException("Initial connection to host url: " + hostUrl + " failed.");
        }

        System.out.println("Initial connection to host url: " + hostUrl + " success");
        System.out.println("Initial run...");
        client.initialRun();
//        client.startStopwatch();
//        CountDownLatch latch = new CountDownLatch(threadCount * groupCount);
//        IntStream.range(0, groupCount).forEach(i -> {
//            System.out.println("Processing group: " + i + "...");
//            client.processGroup(latch);
//            try {
//                Thread.sleep(delayInSeconds * 1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            System.out.println("Something went wrong in Main.processGroup()");
//            throw new RuntimeException(e);
//        }
//        client.stopStopwatch();
//        client.reportStatistics();
    }
}