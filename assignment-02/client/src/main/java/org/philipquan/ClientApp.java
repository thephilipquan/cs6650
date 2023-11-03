package org.philipquan;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

    private static final String FORM_IMAGE_KEY = "image";
    private static final String FORM_ALBUM_INFO_KEY = "profile";
    private static final Pattern ALBUMID_PATTERN = Pattern.compile("albumId.*?(\\d+)");
    private static final String ENDPOINT = "/albums";
    private static final Integer INITIAL_THREAD_COUNT = 10;
    private static final Integer INITIAL_REQUEST_COUNT = 100;
    private static final Integer GROUP_REQUEST_COUNT = 1000;
    private static final Integer REQUEST_RETRY_COUNT = 5;

    private final Integer groupThreadCount;
    private final Integer groupCount;
    private final Integer delayInSeconds;
    private final String hostUrl;
    private final String image;
    private final List<RequestStatistic> methodStatistics;
    private final CloseableHttpClient client;

    public ClientApp(Integer groupThreadCount, Integer groupCount, Integer delayInSeconds, String hostUrl, String image, List<RequestStatistic> requestStatistics) {
        this.groupThreadCount = groupThreadCount;
        this.groupCount = groupCount;
        this.delayInSeconds = delayInSeconds;
        this.hostUrl = hostUrl;
        this.image = image;
        this.methodStatistics = requestStatistics;
        this.client = HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(REQUEST_RETRY_COUNT, true))
          .build();
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists() {
        HttpGet request = new HttpGet(this.hostUrl + ENDPOINT);
        try {
            int statusCode = this.client.execute(request).getStatusLine().getStatusCode();
            return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
        } catch (IOException e) {
            return false;
        }
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(INITIAL_THREAD_COUNT);
        processThreads(INITIAL_THREAD_COUNT, INITIAL_REQUEST_COUNT, latch);
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
            processThreads(this.groupThreadCount, GROUP_REQUEST_COUNT, latch);
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
        IntStream.range(0, requestCount).forEach(j -> {
//            Integer albumId = postAlbum();
            Integer albumId = 1;
            getAlbum(albumId);
        });
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum() {
        HttpPost request = new HttpPost(this.hostUrl + ENDPOINT);

        HttpEntity entity = MultipartEntityBuilder.create()
          .addPart(FORM_IMAGE_KEY, new StringBody(image, ContentType.TEXT_PLAIN))
          .addPart(FORM_ALBUM_INFO_KEY, new StringBody("{\"artist\": \"joe\", \"title\": \"joe's story\", \"year\": 2023}", ContentType.TEXT_PLAIN))
          .build();
        request.setEntity(entity);

        Timer timer = new Timer();
        String responseBody;
        CloseableHttpResponse response = null;
        try {
            timer.start();
            response = this.client.execute(request);
            timer.stop();
            responseBody = EntityUtils.toString(response.getEntity());
            this.methodStatistics.add(RequestStatistic.createPost(response.getStatusLine().getStatusCode(), timer));
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

    private void getAlbum(Integer albumId) {
        HttpGet request = new HttpGet(this.hostUrl + ENDPOINT + "?albumId=" + albumId);
        Timer timer = new Timer();
        CloseableHttpResponse response = null;
        try {
            timer.start();
            response = this.client.execute(request);
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
        try {
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reportStatistics(Timer timer) {
        final double runtimeInSeconds = (double) timer.getElapsedTime() / 1000;
        final long throughput = Math.round((this.groupCount * this.groupThreadCount * GROUP_REQUEST_COUNT
          * 2) / runtimeInSeconds);
        System.out.println("Wall Time: " + runtimeInSeconds + " second(s)");
        System.out.println("Throughput: " + throughput + " requests per second");

        this.methodStatistics.sort(Comparator.comparingLong(RequestStatistic::getLatency));
        final long minLatency = this.methodStatistics.get(0).getLatency();
        System.out.println("Min latency: " + minLatency);

        final long maxLatency = this.methodStatistics.get(this.methodStatistics.size() - 1).getLatency();
        System.out.println("Max latency: " + maxLatency);

        final long meanLatency = this.methodStatistics.stream()
          .map(RequestStatistic::getLatency)
          .reduce(0L, Long::sum) / methodStatistics.size();
        System.out.println("Mean latency: " + meanLatency);

        final int middleIndex = this.methodStatistics.size() / 2;
        final long medianLatency = this.methodStatistics.get(middleIndex).getLatency();
        System.out.println("Median latency: " + medianLatency);

        final int percentileIndex = (int) Math.ceil(0.99 * this.methodStatistics.size());
        if (percentileIndex >= this.methodStatistics.size()) {
            System.out.println("percentile index is too large");
        } else {
            final long percentile99Latency = this.methodStatistics.get(percentileIndex - 1).getLatency();
            System.out.println("99th Percentile latency: " + percentile99Latency);
        }
    }
}