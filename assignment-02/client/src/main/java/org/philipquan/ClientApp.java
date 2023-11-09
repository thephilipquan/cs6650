package org.philipquan;

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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;


public class ClientApp {

    private final Integer groupThreadCount;
    private final Integer groupCount;
    private final Integer delayInSeconds;
    private final String hostUrl;
    private final String image;
    private final List<RequestStatistic> methodStatistics;

    public ClientApp(Integer groupThreadCount, Integer groupCount, Integer delayInSeconds, String hostUrl, String image, List<RequestStatistic> requestStatistics) {
        this.groupThreadCount = groupThreadCount;
        this.groupCount = groupCount;
        this.delayInSeconds = delayInSeconds;
        this.hostUrl = hostUrl;
        this.image = image;
        this.methodStatistics = requestStatistics;
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists() {
        HttpGet request = new HttpGet(this.hostUrl + Main.ENDPOINT);
        try (
          CloseableHttpClient client = createClient();
          ) {
            int statusCode = client.execute(request).getStatusLine().getStatusCode();
            return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
        } catch (IOException e) {
            return false;
        }
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(Main.INITIAL_THREAD_COUNT);
        processThreads(Main.INITIAL_THREAD_COUNT, Main.INITIAL_REQUEST_COUNT, latch);
        try {
            latch.await();
            System.out.println("Initial run completed.");
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in initialRun()");
            throw new RuntimeException(e);
        }
    }

    public void groupRun() {
        CountDownLatch latch = new CountDownLatch(this.groupCount * this.groupThreadCount);
        IntStream.range(0, this.groupCount).forEach(i -> {
            System.out.println("Processing group: " + i + "...");
            processThreads(this.groupThreadCount, Main.GROUP_REQUEST_COUNT, latch);
            try {
                Thread.sleep(this.delayInSeconds * 1000);
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
    public void processThreads(int threadCount, int requestCount, CountDownLatch latch) {
            IntStream.range(0, threadCount).forEach(i -> {
                Runnable instruction = () -> {
                    callServer(requestCount);
                    latch.countDown();
                };
                new Thread(instruction).start();
            });
    }

    private void callServer(int requestCount) {
        try (
          CloseableHttpClient client = createClient();
        ) {
            IntStream.range(0, requestCount).forEach(j -> {
                Integer albumId = postAlbum(client);
                getAlbum(client, albumId);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum(CloseableHttpClient client) {
        HttpPost request = new HttpPost(this.hostUrl + Main.ENDPOINT);

        HttpEntity entity = MultipartEntityBuilder.create()
          .addPart(Main.FORM_IMAGE_KEY, new StringBody(image, ContentType.TEXT_PLAIN))
          .addPart(Main.FORM_ALBUM_INFO_KEY, new StringBody("{\"artist\": \"joe\", \"title\": \"joe's story\", \"year\": 2023}", ContentType.TEXT_PLAIN))
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
            this.methodStatistics.add(RequestStatistic.createPost(response.getStatusLine().getStatusCode(), timer));
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
        HttpGet request = new HttpGet(this.hostUrl + Main.ENDPOINT + "?albumId=" + albumId);
        Timer timer = new Timer();
        CloseableHttpResponse response = null;
        try {
            timer.start();
            response = client.execute(request);
            timer.stop();
            this.methodStatistics.add(RequestStatistic.createGet(response.getStatusLine().getStatusCode(), timer));
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

    private CloseableHttpClient createClient() {
        return HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(Main.REQUEST_RETRY_COUNT, true))
          .build();
    }
}