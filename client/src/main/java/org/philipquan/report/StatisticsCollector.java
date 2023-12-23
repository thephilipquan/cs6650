package org.philipquan.report;

import java.util.ArrayList;
import java.util.List;

public class StatisticsCollector {

    private final List<RequestStatistic> statistics;
    private final Timer timer;

    public StatisticsCollector() {
        this.statistics = new ArrayList<>();
        this.timer = new Timer();
    }

    public synchronized void add(RequestStatistic statistic) {
        this.statistics.add(statistic);
    }

    public List<RequestStatistic> getList() {
        return this.statistics;
    }

    public int getCount() {
        return this.statistics.size();
    }

    public void startTimer() {
        this.timer.start();
    }

    public void stopTimer() {
        this.timer.stop();
    }

    public Timer getTimer() {
        return this.timer;
    }
}