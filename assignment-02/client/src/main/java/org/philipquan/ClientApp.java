package org.philipquan;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;


public class ClientApp {

    private final Integer threadCount;
    private final Integer groupCount;
    private final Integer delayInSeconds;
    private final String hostUrl;
    private final String image;
    private final List<RequestStatistic> methodStatistics;
    private long startTime;
    private long endTime;

    private static final String ENDPOINT = "/albums";
    private static final Integer INITIAL_RUN_THREAD_COUNT = 1; // 10
    private static final Integer INITIAL_RUN_LOOP_COUNT = 1; // 100
    private static final Integer SERVER_METHOD_CALL_COUNT = 1000;
    private static final Integer METHOD_RETRY_COUNT = 5;

    public ClientApp(Integer threadCount, Integer groupCount, Integer delayInSeconds, String hostUrl, String image, List methodStatistics) {
        this.threadCount = threadCount;
        this.groupCount = groupCount;
        this.delayInSeconds = delayInSeconds;
        this.hostUrl = hostUrl;
        this.image = image;
        this.methodStatistics = methodStatistics;
    }

    /**
     * @return {@code true} if a GET request to {@code this.hostUrl} returns a status code between
     * {@link HttpStatus#SC_OK} and {@link HttpStatus#SC_BAD_REQUEST}.
     */
    public Boolean hostUrlExists() {
        HttpClient client = HttpClientBuilder.create()
          .setRetryHandler(new StandardHttpRequestRetryHandler(METHOD_RETRY_COUNT, true))
          .build();
        HttpGet request = new HttpGet(this.hostUrl + ENDPOINT);
        try {
            int statusCode = client.execute(request).getStatusLine().getStatusCode();
            return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_BAD_REQUEST;
        } catch (IOException e) {
            return false;
        }
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(INITIAL_RUN_THREAD_COUNT);
        processThreads(INITIAL_RUN_THREAD_COUNT, INITIAL_RUN_LOOP_COUNT, latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in initialRun()");
            throw new RuntimeException(e);
        }
    }

    public void groupRun() {
        CountDownLatch latch = new CountDownLatch(threadCount * groupCount);
        IntStream.range(0, groupCount).forEach(i -> {
            System.out.println("Processing group: " + i + "...");
            processThreads(this.threadCount, SERVER_METHOD_CALL_COUNT, latch);
            try {
                Thread.sleep(this.delayInSeconds * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in processGroup()");
            throw new RuntimeException(e);
        }
    }
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
//        HttpClient client = HttpClientBuilder.create()
//          .setRetryHandler(new StandardHttpRequestRetryHandler(METHOD_RETRY_COUNT, true))
//          .build();
        IntStream.range(0, requestCount).forEach(j -> {
            Integer albumId = postAlbum();
            getAlbum(albumId);
        });
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum() {
//        try {
//            String body = "image=" + this.image + "," + "profile={\"artist\": \"Sex Pistols\", \"title\": \"Never Mind the Bollocks\", \"year\": 1983}";
//            HttpRequest request = HttpRequest.newBuilder()
//              .uri(URI.create(this.hostUrl))
//              .header("Content-Type", "multipart/form-data")
//              .POST(BodyPublishers.ofString(body))
//              .build();
//            URL url = new URI(this.hostUrl).toURL();
//            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "multipart/form-data");
//            connection.setDoOutput(true);
//            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
//
//            stream.writeBytes(body);
//            stream.flush();
//            stream.close();
//        } catch (URISyntaxException | IOException e) {
//            throw new RuntimeException(e);
//        }

//        HttpPost post = new HttpPost(this.hostUrl + POST_PATH);
//        HttpEntity entity = MultipartEntityBuilder.create()
//          .addTextBody("image", "abcde")
//          .addTextBody("profile", "{\"artist\": \"Sex Pistols\", \"title\": \"Never Mind the Bollocks\", \"year\": 1983}")
//            .build();
//        post.setEntity(entity);
//        final long startRequestTime = System.currentTimeMillis();
//        HttpResponse response = null;
//        try {
//            response = client.execute(post);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        final long requestLatency = System.currentTimeMillis() - startRequestTime;
//
//        int statusCode = response.getStatusLine().getStatusCode();
//        this.methodStatistics.add(new MethodStatistic(startRequestTime, post.getClass().getSimpleName(), requestLatency, statusCode));
//
//        System.out.println(response.getEntity());
        return -1;
    }

    private void getAlbum(HttpClient client, Integer albumId) {
        HttpGet request = new HttpGet(this.hostUrl + ENDPOINT);
        RequestTimer timer = new RequestTimer();
        HttpResponse response = null;
        try {
            timer.start();
            response = client.execute(request);
            timer.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.methodStatistics.add(RequestStatistic.createGet(response.getStatusLine().getStatusCode(), timer));
    }

//    private void callServerMethod(final HttpClient client, final HttpMethod method) {
//        try {
//            final long startRequestTime = System.currentTimeMillis();
//            Integer statusCode = client.executeMethod(method);
//            final long requestLatency = System.currentTimeMillis() - startRequestTime;
//            this.methodStatistics.add(new MethodStatistic(startRequestTime, method.getClass().getSimpleName(), requestLatency, statusCode));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            method.releaseConnection();
//        }
//    }

    public void startStopwatch() {
        this.startTime = System.currentTimeMillis();
    }

    public void stopStopwatch() {
        this.endTime = System.currentTimeMillis();
    }

    public void reportStatistics() {
        final double runtimeInSeconds = (double) (this.endTime - this.startTime) / 1000;
        final Long throughput = Math.round((this.groupCount * this.threadCount * SERVER_METHOD_CALL_COUNT * 2) / runtimeInSeconds);
        System.out.println("Wall Time: " + runtimeInSeconds + " second(s)");
        System.out.println("Throughput: " + throughput + " requests per second");

        this.methodStatistics.sort(Comparator.comparingLong(RequestStatistic::getLatency));
        final long minLatency = this.methodStatistics.get(0).getLatency();
        System.out.println("Min latency: " + minLatency);

        final long maxLatency = this.methodStatistics.get(this.methodStatistics.size() - 1).getLatency();
        System.out.println("Max latency: " + maxLatency);

        final double meanLatency = this.methodStatistics.stream().map(method -> method.getLatency()).reduce(0L, Long::sum) / methodStatistics.size();
        System.out.println("Mean latency: " + meanLatency);

        final int middleIndex = (int) Math.floor(this.methodStatistics.size() / 2);
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