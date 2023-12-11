package org.philipquan.connection;

import static org.philipquan.RunConfig.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.philipquan.model.HttpPostAlbum;
import org.philipquan.model.HttpPostReaction;
import org.philipquan.report.RequestStatistic;
import org.philipquan.RunConfig;
import org.philipquan.report.StatisticsCollector;
import org.philipquan.report.Timer;


public class PostApp {
    private final RunConfig config;
    private final ClientManager clientManager;
    private final StatisticsCollector collector;

    public PostApp(RunConfig config, ClientManager clientManager, StatisticsCollector collector) {
        this.config = config;
        this.clientManager = clientManager;
        this.collector = collector;
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(INITIAL_THREAD_COUNT);
        createThreadsAndRequest(INITIAL_THREAD_COUNT, INITIAL_REQUEST_COUNT, latch, false);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Something went wrong in initialRun()", e);
        }
    }

    public void groupRun() {
        CountDownLatch latch = new CountDownLatch(this.config.getGroupCount() * this.config.getGroupThreadCount());
        this.collector.startTimer();
        for (int i = 0; i < this.config.getGroupCount(); i++) {
            System.out.println("Processing group: " + i + "...");
            createThreadsAndRequest(this.config.getGroupThreadCount(), GROUP_REQUEST_COUNT, latch, true);
            try {
                Thread.sleep(this.config.getDelayInSeconds() * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            latch.await();
            this.collector.stopTimer();
            System.out.println("Processing group completed.");
        } catch (InterruptedException e) {
            throw new RuntimeException("Something went wrong in processGroup()", e);
        }
    }

    /**
     * @param threadCount the amount of threads to create. Each thread will call the {@code hostUrl}
     *                    {@code requestCount} amount of times.
     * @param requestCount the amount of times to call the {@code hostUrl}
     * @param latch the countdown latch to decrement every time a thread is finished calling
     *                    {@code requestCount} amount of times
     */
    public void createThreadsAndRequest(int threadCount, int requestCount, CountDownLatch latch, boolean collectStats) {
            for (int i = 0; i < threadCount; i++) {
                Runnable instruction = () -> {
                    CloseableHttpClient client = this.clientManager.getClient();
                    for (int j = 0; j < requestCount; j++) {
                        Integer albumId = postAlbum(client, collectStats);
                        reactToAlbum(client, albumId, collectStats);
                    }
                    latch.countDown();
                };
                new Thread(instruction).start();
            }
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum(CloseableHttpClient client, boolean collectStats) {
        HttpPost request = new HttpPostAlbum(this.config.getHostUrl());

        HttpEntity entity = MultipartEntityBuilder.create()
          .addPart(FORM_IMAGE_KEY, this.config.getImage())
          .addPart(FORM_ALBUM_INFO_KEY, this.config.getAlbumInfo())
          .build();
        request.setEntity(entity);

        Timer timer = new Timer();
        String responseBody;
        CloseableHttpResponse response = null;
        try {
            timer.start();
            response = client.execute(request);
            timer.stop();
            responseBody = EntityUtils.toString(response.getEntity());
            // condition so we don't collect stats for initialRun.
            if (collectStats) {
                this.collector.add(RequestStatistic.createPostAlbum(response.getStatusLine().getStatusCode(), timer));
            }
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            request.releaseConnection();
            safeCloseResponse(response);
        }

        Matcher match = ALBUMID_PATTERN.matcher(responseBody);
        if (!match.find()) {
            throw new RuntimeException(responseBody);
        }
        return Integer.parseInt(match.group(1));
    }

    private void reactToAlbum(CloseableHttpClient client, Integer albumId, boolean collectStats) {
        HttpPost likeRequest = new HttpPostReaction(this.config.getHostUrl(), LIKE_ENDPOINT, albumId);
        HttpPost dislikeRequest = new HttpPostReaction(this.config.getHostUrl(), DISLIKE_ENDPOINT, albumId);
        List<HttpPost> requests = new ArrayList<>();
        for (int i = 0; i < POST_REACTION_LIKE_COUNT; i++) {
            requests.add(likeRequest);
        }
        for (int i = 0; i < POST_REACTION_DISLIKE_COUNT; i++) {
            requests.add(dislikeRequest);
        }

        for (HttpPost request : requests) {
            CloseableHttpResponse response = null;
            Timer timer = new Timer();
            try {
                timer.start();
                response = client.execute(request);
                timer.stop();
                // condition so we don't collect stats for initialRun.
                if (collectStats) {
                    this.collector.add(RequestStatistic.createPostReaction(response.getStatusLine().getStatusCode(), timer));
                }
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                safeCloseResponse(response);
            }
        }
        likeRequest.releaseConnection();
        dislikeRequest.releaseConnection();
    }

    // todo delete
    private void safeCloseResponse(CloseableHttpResponse response) {
    }
}