package org.philipquan;


import org.philipquan.connection.GetApp;
import org.philipquan.connection.PostApp;
import org.philipquan.connection.ClientManager;
import org.philipquan.model.Lambda;
import org.philipquan.report.FileStatisticsReport;
import org.philipquan.report.StatisticsCollector;

public class Main {

    public static void main(final String[] args) {
        RunConfig config = new RunConfig(RunConfig.RUN_CONFIG_FILENAME);
        ClientManager clientManager = new ClientManager();
        if (!clientManager.hostUrlExists(config.getHostUrl())) {
            throw new RuntimeException(String.format("Initial connection to host url: %s failed.", config.getHostUrl()));
        }
        System.out.println(String.format("Initial connection to host url: %s success.", config.getHostUrl()));

        StatisticsCollector postCollector = new StatisticsCollector();
        PostApp client = new PostApp(config, clientManager, postCollector);

        StatisticsCollector getCollector = new StatisticsCollector();
        GetApp getApp = new GetApp(config, clientManager, getCollector);

        System.out.println("Initial run...");
        client.initialRun();
        System.out.println("Initial run completed.");

        final int threshold = RunConfig.GROUP_REQUEST_COUNT * 2;
        executeAtStatisticsThreshold(() -> getApp.startThreads(), postCollector, threshold);

        client.groupRun();
        getApp.stopThreads();

        FileStatisticsReport report = new FileStatisticsReport();
        System.out.println("post count: " + postCollector.getCount());
        System.out.println("get count: " + getCollector.getCount());
        report.out(config, postCollector, getCollector);
    }

    private static void executeAtStatisticsThreshold(Lambda callback, StatisticsCollector collector, int threshold) {
        Runnable instruction = () -> {
            while (collector.getCount() < threshold) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            callback.execute();
        };
        new Thread(instruction).start();
    }
}