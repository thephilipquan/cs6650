package org.philipquan.connection;

import org.philipquan.RunConfig;
import org.philipquan.connection.runnable.GetLastAlbumIdRunnable;
import org.philipquan.connection.runnable.GetRandomAlbumIdRunnable;
import org.philipquan.model.SynchronizedInteger;
import org.philipquan.report.StatisticsCollector;

public class GetApp {

    private final RunConfig config;
    private final ClientManager clientManager;
    private final StatisticsCollector collector;
    private final Thread[] threads;
    private final Thread getLastAlbumIdThread;
    private final SynchronizedInteger lastAlbumId;

    public GetApp(RunConfig config, ClientManager clientManager, StatisticsCollector collector) {
        this.config = config;
        this.clientManager = clientManager;
        this.collector = collector;

        this.lastAlbumId = new SynchronizedInteger();
        this.threads = new Thread[RunConfig.GET_THREAD_COUNT];
        this.getLastAlbumIdThread = new Thread(
          new GetLastAlbumIdRunnable(
            this.clientManager,
            this.config.getHostUrl(),
            this.lastAlbumId,
            RunConfig.GET_RANDOM_ALBUM_ID_DELAY_IN_SECONDS
        ));
    }

    public void startThreads() {
        System.out.println(String.format("Starting %d GET threads...", RunConfig.GET_THREAD_COUNT));
        this.getLastAlbumIdThread.start();
        this.collector.startTimer();
        for (int i = 0; i < RunConfig.GET_THREAD_COUNT; i++) {
            Runnable instruction = new GetRandomAlbumIdRunnable(
              this.clientManager.getClient(),
              this.config.getHostUrl(),
              this.lastAlbumId,
              this.collector
            );
            this.threads[i] = new Thread(instruction);
            this.threads[i].start();
        }
    }

    public void stopThreads() {
        for (Thread thread : this.threads) {
            thread.interrupt();
        }
        this.getLastAlbumIdThread.interrupt();
        this.collector.stopTimer();
    }


}