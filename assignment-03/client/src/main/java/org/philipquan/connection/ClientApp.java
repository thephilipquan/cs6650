package org.philipquan.connection;

import static org.philipquan.RunConfig.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.philipquan.model.HttpPostAlbum;
import org.philipquan.model.HttpPostReaction;
import org.philipquan.report.RequestStatistic;
import org.philipquan.RunConfig;
import org.philipquan.report.Timer;


public class ClientApp {
    private final RunConfig config;
    private final ClientManager clientManager;
    private final List<RequestStatistic> requestStatistics;

    public ClientApp(RunConfig config, ClientManager clientManager, List<RequestStatistic> requestStatistics) {
        this.config = config;
        this.clientManager = clientManager;
        this.requestStatistics = requestStatistics;
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists() {
        HttpGet request = new HttpGet(String.format("%s/%s", this.config.getHostUrl(), ALBUM_ENDPOINT));
        int statusCode;
        try {
            statusCode = this.clientManager.getClient().execute(request).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return false;
        }
        return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(INITIAL_THREAD_COUNT);
        createThreadsAndRequest(INITIAL_THREAD_COUNT, INITIAL_REQUEST_COUNT, latch);
        try {
            latch.await();
            System.out.println("Initial run completed.");
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in initialRun()");
            throw new RuntimeException(e);
        }
    }

    public void groupRun() {
        CountDownLatch latch = new CountDownLatch(this.config.getGroupCount() * this.config.getGroupThreadCount());
        IntStream.range(0, this.config.getGroupCount()).forEach(i -> {
            System.out.println("Processing group: " + i + "...");
            createThreadsAndRequest(this.config.getGroupThreadCount(), GROUP_REQUEST_COUNT, latch);
            try {
                Thread.sleep(this.config.getDelayInSeconds() * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            latch.await();
            System.out.println("Processing group completed.");
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in processGroup()");
            throw new RuntimeException(e);
        }
    }

    /**
     * @param threadCount the amount of threads to create. Each thread will call the {@code hostUrl}
     *                    {@code requestCount} amount of times.
     * @param requestCount the amount of times to call the {@code hostUrl}
     * @param latch the countdown latch to decrement every time a thread is finished calling
     *                    {@code requestCount} amount of times
     */
    public void createThreadsAndRequest(int threadCount, int requestCount, CountDownLatch latch) {
            IntStream.range(0, threadCount).forEach(i -> {
                Runnable instruction = () -> {
                    CloseableHttpClient client = this.clientManager.getClient();
                    IntStream.range(0, requestCount).forEach(j -> {
                        Integer albumId = postAlbum(client);
                        reactToAlbum(client, albumId);
                    });
                    latch.countDown();
                };
                new Thread(instruction).start();
            });
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum(CloseableHttpClient client) {
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
            this.requestStatistics.add(RequestStatistic.createPostAlbum(response.getStatusLine().getStatusCode(), timer));
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

    private void reactToAlbum(CloseableHttpClient client, Integer albumId) {
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
                this.requestStatistics.add(RequestStatistic.createPostReaction(response.getStatusLine().getStatusCode(), timer));
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

    private void safeCloseResponse(CloseableHttpResponse response) {
    }
}