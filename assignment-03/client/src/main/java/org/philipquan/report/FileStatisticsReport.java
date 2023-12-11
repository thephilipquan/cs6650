package org.philipquan.report;

import static org.philipquan.RunConfig.ALBUM_ENDPOINT;
import static org.philipquan.RunConfig.REACTION_ENDPOINT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.philipquan.RunConfig;

public class FileStatisticsReport {

    public void out(RunConfig config, StatisticsCollector postCollector, StatisticsCollector getCollector) {
        StringJoiner report = new StringJoiner("\n");

        String fileName = String.format("%s-groupsize%d-out.txt", config.getOutPrefix(),
          config.getGroupCount());
        report.add(String.format("# %s", fileName));
        report.add(""); // newline.

        summarize(postCollector.getList(), postCollector.getTimer(), report);
        detail(
          String.format("POST %s", ALBUM_ENDPOINT),
          postCollector.getList().stream()
            .filter((r) -> r.getEndpoint().equals(ALBUM_ENDPOINT))
            .collect(Collectors.toList()),
          report
        );
        detail(
          String.format("POST %s", REACTION_ENDPOINT),
          postCollector.getList().stream()
            .filter((r) -> r.getEndpoint().equals(REACTION_ENDPOINT))
            .collect(Collectors.toList()),
          report
        );

        summarize(getCollector.getList(), getCollector.getTimer(), report);
        detail(String.format("GET %s", REACTION_ENDPOINT), getCollector.getList(), report);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("report/" + fileName));
            writer.write(report.toString());
            System.out.println("Report saved as " + fileName);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void summarize(List<RequestStatistic> stats, Timer timer, StringJoiner report) {
        report.add(createHeader("POST Summary"));
        final double runtimeInSeconds = (double) timer.getElapsedTime() / 1000; // to seconds.
        final long throughput = Math.round(stats.size() / runtimeInSeconds);
        report.add("Wall Time: " + runtimeInSeconds + " second(s)");
        report.add("Throughput: " + throughput + " requests per second");

        List<RequestStatistic> passed = new ArrayList<>();
        for (int i = 0; i < stats.size(); i++) {
            if (stats.get(i) == null) {
                System.out.println("null at " + i);
                throw new RuntimeException();
            }
        }
        List<RequestStatistic> failed = new ArrayList<>();
        for (RequestStatistic r : stats) {
            if (r.getStatusCode() <= HttpStatus.SC_CREATED) {
                failed.add(r);
            }
        }
//        List<RequestStatistic> passed = stats.stream()
//          .filter((r) -> r.getStatusCode() <= HttpStatus.SC_CREATED)
//          .collect(Collectors.toList());
//        List<RequestStatistic> failed = stats.stream()
//          .filter((r) -> r.getStatusCode() > HttpStatus.SC_CREATED)
//          .collect(Collectors.toList());
        report.add("success " + passed.size());
        report.add("failed: " + failed.size());
    }

    private static void detail(String header, List<RequestStatistic> stats, StringJoiner report) {
        report.add(""); // newline.
        report.add(createHeader(header));
        report.add(""); // newline.
        if (stats.size() == 0) {
            report.add(String.format("No %s statistics.", header));
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