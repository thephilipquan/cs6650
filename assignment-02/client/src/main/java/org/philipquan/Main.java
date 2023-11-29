package org.philipquan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.philipquan.connection.ClientApp;
import org.philipquan.connection.ClientManager;
import org.philipquan.utility.RequestStatistic;
import org.philipquan.utility.RunConfiguration;
import org.philipquan.utility.Timer;

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
        RunConfiguration config = new RunConfiguration("run.conf");
        ClientManager clientManager = new ClientManager();
        List<RequestStatistic> requestStatistics = Collections.synchronizedList(new ArrayList<>());
        ClientApp client = new ClientApp(config, clientManager, requestStatistics);

        if (!client.hostUrlExists()) {
            throw new RuntimeException(String.format("Initial connection to host url: %s failed.", config.getHostUrl()));
        }

        System.out.println(String.format("Initial connection to host url: %s success.", config.getHostUrl()));
        System.out.println("Initial run...");
        client.initialRun();

        Timer timer = new Timer();
        timer.start();
        client.groupRun();
        timer.stop();

        reportStatistics(requestStatistics, config, timer);
    }

    private static void reportStatistics(List<RequestStatistic> stats, RunConfiguration config, Timer timer) {
        StringJoiner report = new StringJoiner("\n");

        String fileName = String.format("%s-groupsize%d-out.txt", config.getOutPrefix(), config.getGroupCount());
        report.add(String.format("# %s", fileName));
        report.add(""); // newline.

        report.add(createHeader("Summary"));
        final double runtimeInSeconds = (double) timer.getElapsedTime() / 1000; // to seconds.
        final long throughput = Math.round((config.getGroupCount() * config.getGroupThreadCount() * GROUP_REQUEST_COUNT * 2) / runtimeInSeconds);
        report.add("Wall Time: " + runtimeInSeconds + " second(s)");
        report.add("Throughput: " + throughput + " requests per second");

        List<RequestStatistic> passed = stats.stream()
          .filter((r) -> r.getStatusCode() <= HttpStatus.SC_CREATED)
          .collect(Collectors.toList());
        List<RequestStatistic> failed = stats.stream()
          .filter((r) -> r.getStatusCode() > HttpStatus.SC_CREATED)
          .collect(Collectors.toList());
        report.add("success " + passed.size());
        report.add("failed: " + failed.size());

        reportRequestType(
          "POST",
          stats.stream()
            .filter((r) -> r.getRequestType().equals("POST"))
            .collect(Collectors.toList()),
          report);
        reportRequestType(
          "GET",
          stats.stream()
            .filter((r) -> r.getRequestType().equals("GET"))
            .collect(Collectors.toList()),
          report);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("report/" + fileName));
            writer.write(report.toString());
            System.out.println("Report saved as " + fileName);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reportRequestType(String type, List<RequestStatistic> stats, StringJoiner report) {
        report.add(""); // newline.
        report.add(createHeader(type));
        report.add(""); // newline.
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
        return String.format("## %s", title);
    }
}