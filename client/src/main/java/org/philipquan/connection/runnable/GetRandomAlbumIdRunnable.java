package org.philipquan.connection.runnable;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.philipquan.model.HttpGetAlbumId;
import org.philipquan.model.SynchronizedInteger;
import org.philipquan.report.RequestStatistic;
import org.philipquan.report.StatisticsCollector;
import org.philipquan.report.Timer;

public class GetRandomAlbumIdRunnable implements Runnable {

    private final CloseableHttpClient client;
    private final String hostUrl;
    private final SynchronizedInteger lastAlbumId;
    private final StatisticsCollector collector;

    public GetRandomAlbumIdRunnable(CloseableHttpClient client, String hostUrl, SynchronizedInteger lastAlbumId, StatisticsCollector collector) {
        this.client = client;
        this.hostUrl = hostUrl;
        this.lastAlbumId = lastAlbumId;
        this.collector = collector;
    }

    @Override
    public void run() {
        final int min = 1;
        while (!Thread.currentThread().isInterrupted()) {
            Integer albumId = (int) (Math.random() * (this.lastAlbumId.getValue() - min) + min);
            HttpGet request = new HttpGetAlbumId(this.hostUrl, albumId);
            Timer timer = new Timer();
            CloseableHttpResponse response = null;
            try {
                timer.start();
                response = client.execute(request);
                timer.stop();
                this.collector.add(RequestStatistic.createGetReaction(response.getStatusLine().getStatusCode(), timer));
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                request.releaseConnection();
            }
        }
    }
}