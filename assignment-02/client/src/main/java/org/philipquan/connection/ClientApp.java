package org.philipquan.connection;

import java.io.IOException;
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
import org.philipquan.Main;
import org.philipquan.utility.RequestStatistic;
import org.philipquan.utility.RunConfiguration;
import org.philipquan.utility.Timer;


public class ClientApp {
    private final RunConfiguration config;
    private final ClientManager clientManager;
    private final List<RequestStatistic> requestStatistics;

    public ClientApp(RunConfiguration config, ClientManager clientManager, List<RequestStatistic> requestStatistics) {
        this.config = config;
        this.clientManager = clientManager;
        this.requestStatistics = requestStatistics;
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists() {
        HttpGet request = new HttpGet(this.config.getHostUrl() + Main.ENDPOINT);
        int statusCode;
        try {
            statusCode = this.clientManager.getClient().execute(request).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return false;
        }
        return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(Main.INITIAL_THREAD_COUNT);
        createThreadsAndRequest(Main.INITIAL_THREAD_COUNT, Main.INITIAL_REQUEST_COUNT, latch);
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
            createThreadsAndRequest(this.config.getGroupThreadCount(), Main.GROUP_REQUEST_COUNT, latch);
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
                        getAlbum(client, albumId);
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
        HttpPost request = new HttpPost(this.config.getHostUrl() + Main.ENDPOINT);

        HttpEntity entity = MultipartEntityBuilder.create()
          .addPart(Main.FORM_IMAGE_KEY, this.config.getImage())
          .addPart(Main.FORM_ALBUM_INFO_KEY, this.config.getAlbumInfo())
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
            this.requestStatistics.add(RequestStatistic.createPost(response.getStatusLine().getStatusCode(), timer));
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            request.releaseConnection();
            safeCloseResponse(response);
        }

        Matcher match = Main.ALBUMID_PATTERN.matcher(responseBody);
        if (!match.find()) {
            throw new RuntimeException(responseBody);
        }
        return Integer.parseInt(match.group(1));
    }

    private void getAlbum(CloseableHttpClient client, Integer albumId) {
        HttpGet request = new HttpGet(this.config.getHostUrl() + Main.ENDPOINT + "?albumId=" + albumId);
        Timer timer = new Timer();
        CloseableHttpResponse response = null;
        try {
            timer.start();
            response = client.execute(request);
            timer.stop();
            this.requestStatistics.add(RequestStatistic.createGet(response.getStatusLine().getStatusCode(), timer));
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            request.releaseConnection();
            safeCloseResponse(response);
        }
    }

    private void safeCloseResponse(CloseableHttpResponse response) {
        if (response == null)
            return;
        try {
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}