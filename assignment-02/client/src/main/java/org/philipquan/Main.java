package org.philipquan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    public static final String FORM_IMAGE_KEY = "image";
    public static final String FORM_ALBUM_INFO_KEY = "profile";
    public static final Pattern ALBUMID_PATTERN = Pattern.compile("albumId.*?(\\d+)");
    public static final String ENDPOINT = "/albums";
    public static final Integer INITIAL_THREAD_COUNT = 10;
    public static final Integer INITIAL_REQUEST_COUNT = 100;
    public static final Integer GROUP_REQUEST_COUNT = 1000;
    public static final Integer REQUEST_RETRY_COUNT = 5;

    public static void main(final String[] args) {
        if (args.length != 5) {
            throw new RuntimeException("Please provide 5 arguments to run the program. (groupThreadCount, groupCount, delayInSeconds, hostUrl, imagePath)");
        }

        final int groupThreadCount = Integer.parseInt(args[0]);
        final int groupCount = Integer.parseInt(args[1]);
        final int delayInSeconds = Integer.parseInt(args[2]);
        final String hostUrl = args[3];
        final String imagePath = args[4];

        String image = readFileAsString(imagePath);
        List<RequestStatistic> requestStatistics = Collections.synchronizedList(new ArrayList<>());
        ClientApp client = new ClientApp(groupThreadCount, groupCount, delayInSeconds, hostUrl, image, requestStatistics);
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
        reportStatistics(requestStatistics, groupCount, groupThreadCount, timer);
    }

    private static String readFileAsString(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reportStatistics(List<RequestStatistic> requestStatistics, int groupCount, int groupThreadCount, Timer timer) {
        final double runtimeInSeconds = (double) timer.getElapsedTime() / 1000;
        final long throughput = Math.round((groupCount * groupThreadCount * GROUP_REQUEST_COUNT
          * 2) / runtimeInSeconds);
        System.out.println("Wall Time: " + runtimeInSeconds + " second(s)");
        System.out.println("Throughput: " + throughput + " requests per second");

        requestStatistics.sort(Comparator.comparingLong(RequestStatistic::getLatency));
        final long minLatency = requestStatistics.get(0).getLatency();
        System.out.println("Min latency: " + minLatency);

        final long maxLatency = requestStatistics.get(requestStatistics.size() - 1).getLatency();
        System.out.println("Max latency: " + maxLatency);

        final long meanLatency = requestStatistics.stream()
          .map(RequestStatistic::getLatency)
          .reduce(0L, Long::sum) / requestStatistics.size();
        System.out.println("Mean latency: " + meanLatency);

        final int middleIndex = requestStatistics.size() / 2;
        final long medianLatency = requestStatistics.get(middleIndex).getLatency();
        System.out.println("Median latency: " + medianLatency);

        final int percentileIndex = (int) Math.ceil(0.99 * requestStatistics.size());
        if (percentileIndex >= requestStatistics.size()) {
            System.out.println("percentile index is too large");
        } else {
            final long percentile99Latency = requestStatistics.get(percentileIndex - 1).getLatency();
            System.out.println("99th Percentile latency: " + percentile99Latency);
        }
    }
}