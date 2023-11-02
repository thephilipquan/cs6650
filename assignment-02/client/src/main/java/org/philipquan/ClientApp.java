package org.philipquan;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

public class ClientApp {

    private final Integer threadCount;
    private final Integer groupCount;
    private final String hostUrl;
    private final String image;
    private final List<MethodStatistic> methodStatistics;
    private long startTime;
    private long endTime;

    private static final String GET_PATH = "/albums";
    private static final String POST_PATH = "/albums";
    private static final Integer INITIAL_RUN_THREAD_COUNT = 1; // 10
    private static final Integer INITIAL_RUN_LOOP_COUNT = 1; // 100
    private static final Integer SERVER_METHOD_CALL_COUNT = 1000;
    private static final Integer METHOD_RETRY_COUNT = 5;

    public ClientApp(Integer threadCount, Integer groupCount, String hostUrl, String image, List methodStatistics) {
        this.threadCount = threadCount;
        this.groupCount = groupCount;
        this.hostUrl = hostUrl;
        this.image = image;
        this.methodStatistics = methodStatistics;
    }

    public Boolean hostUrlExists() {
//        HttpClient client = HttpClientBuilder.create()
//          .setRetryHandler(new StandardHttpRequestRetryHandler(METHOD_RETRY_COUNT, true))
//          .build();
//        HttpGet testConnectionMethod = new HttpGet(this.hostUrl + GET_PATH);
//        try {
//            client.execute(testConnectionMethod);
//        } catch (IOException e) {
//            return false;
//        } finally {
//            testConnectionMethod.releaseConnection();
//        }
        return true;
    }

    public void initialRun() {
        CountDownLatch latch = new CountDownLatch(INITIAL_RUN_THREAD_COUNT);
        IntStream.range(0, INITIAL_RUN_THREAD_COUNT).forEach(i -> {
            Runnable instruction = () -> {
                callServer(INITIAL_RUN_LOOP_COUNT);
                latch.countDown();
            };
            new Thread(instruction).start();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in Main.firstRun()");
            throw new RuntimeException(e);
        }
    }

    public void processGroup(CountDownLatch latch) {
        IntStream.range(0, this.threadCount).forEach(i -> {
            Runnable instruction = () -> {
                callServer(SERVER_METHOD_CALL_COUNT);
                latch.countDown();
            };
            new Thread(instruction).start();
        });
    }

    private void callServer(final int methodCallCount) {
//        HttpClient client = HttpClientBuilder.create()
//          .setRetryHandler(new StandardHttpRequestRetryHandler(METHOD_RETRY_COUNT, true))
//          .build();
        IntStream.range(0, methodCallCount).forEach(j -> {
            Integer albumId = postAlbum();
            getAlbum(albumId);
        });
    }

    /**
     * @return The id of the newly posted album.
     */
    private Integer postAlbum() {
        try {
            URL url = new URI("something").toURL();
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data");
            connection.setDoOutput(true);
            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());

            String body = "image=" + this.image + "," + "profile={\"artist\": \"Sex Pistols\", \"title\": \"Never Mind the Bollocks\", \"year\": 1983}";
            stream.writeBytes(body);
            stream.flush();
            stream.close();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

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

    private void getAlbum(Integer albumId) {
        // TODO
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

        this.methodStatistics.sort(Comparator.comparingLong(MethodStatistic::getLatency));
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