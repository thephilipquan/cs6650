package org.philipquan;

import static java.io.File.separator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        if (args.length != 6) {
            throw new RuntimeException("Please provide 5 arguments to run the program. (groupThreadCount, groupCount, delayInSeconds, hostUrl, imagePath, outputPrefix)");
        }

        final int groupThreadCount = Integer.parseInt(args[0]);
        final int groupCount = Integer.parseInt(args[1]);
        final int delayInSeconds = Integer.parseInt(args[2]);
        final String hostUrl = args[3];
        final String imagePath = args[4];
        final String outputFilePrefix = args[5];

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
        reportStatistics(requestStatistics, groupCount, groupThreadCount, outputFilePrefix, timer);
    }

    private static String readFileAsString(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reportStatistics(List<RequestStatistic> stats, int groupCount,
      int groupThreadCount, String outputFilePrefix, Timer timer) {
        StringJoiner report = new StringJoiner("\n");

        report.add(createHeader("Summary"));
        final double runtimeInSeconds = (double) timer.getElapsedTime() / 1000;
        final long throughput = Math.round((groupCount * groupThreadCount * GROUP_REQUEST_COUNT * 2) / runtimeInSeconds);
        report.add("Wall Time: " + runtimeInSeconds + " second(s)");
        report.add("Throughput: " + throughput + " requests per second");

        List<RequestStatistic> passed = stats.stream().filter((r) -> r.getStatusCode() <= 201).collect(Collectors.toList());
        List<RequestStatistic> failed = stats.stream().filter((r) -> r.getStatusCode() > 201).collect(Collectors.toList());
        report.add("success " + passed.size());
        report.add("failed: " + failed.size());

        reportRequestType("Success", passed, report);
        reportRequestType("Failure", failed, report);

        String fileName = String.format("%s-groupsize%d-out.txt", outputFilePrefix, groupCount);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("report/" + fileName));
            writer.write(report.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reportRequestType(String type, List<RequestStatistic> stats, StringJoiner report) {
        report.add(""); // newline.
        report.add(createHeader(type));
        if (stats.size() == 0) {
            report.add(String.format("No %s statistics.", type));
            return;
        }

        stats.sort(Comparator.comparingLong(RequestStatistic::getLatency));
        final long minLatency = stats.get(0).getLatency();
        report.add("Min latency: " + minLatency);

        final long maxLatency = stats.get(stats.size() - 1).getLatency();
        report.add("Max latency: " + maxLatency);

        final long meanLatency = stats.stream()
          .map(RequestStatistic::getLatency)
          .reduce(0L, Long::sum) / stats.size();
        report.add("Mean latency: " + meanLatency);

        final int middleIndex = stats.size() / 2;
        final long medianLatency = stats.get(middleIndex).getLatency();
        report.add("Median latency: " + medianLatency);

        final int percentileIndex = stats.size() - ((int) Math.ceil(0.99 * stats.size()));
        final long percentile99Latency = stats.get(percentileIndex - 1).getLatency();
        report.add("99th percentile latency: " + percentile99Latency);
    }

    private static String createHeader(String title) {
        final String separator = "==========";
        return String.format("%s %s %s", separator, title, separator);
    }
}