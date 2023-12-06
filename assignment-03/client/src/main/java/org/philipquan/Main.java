package org.philipquan;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.philipquan.connection.ClientApp;
import org.philipquan.connection.ClientManager;
import org.philipquan.report.FileStatisticsReport;
import org.philipquan.report.RequestStatistic;
import org.philipquan.report.Timer;

public class Main {

    public static void main(final String[] args) {
        RunConfig config = new RunConfig("run.conf");
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

        FileStatisticsReport report = new FileStatisticsReport();
        report.out(requestStatistics, config, timer);
    }
}