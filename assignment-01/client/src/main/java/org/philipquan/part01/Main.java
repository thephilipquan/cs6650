package org.philipquan.part01;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Main {

    private final Integer threadCount;
    private final Integer groupCount;
    private final String hostUrl;
    private long startTime;
    private long endTime;

    private static final String POST_PATH = "/albums";
    private static final Integer INITIAL_RUN_THREAD_COUNT = 10;
    private static final Integer INITIAL_RUN_LOOP_COUNT = 100;
    private static final Integer SERVER_METHOD_CALL_COUNT = 1000;
    private static final Integer METHOD_RETRY_COUNT = 5;

    public Main(Integer threadCount, Integer groupCount, String hostUrl) {
        this.threadCount = threadCount;
        this.groupCount = groupCount;
        this.hostUrl = hostUrl;
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
        HttpClient server = new HttpClient();
        IntStream.range(0, methodCallCount).forEach(j -> {
            server.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(METHOD_RETRY_COUNT, false));
            callServerMethod(server, new PostMethod(this.hostUrl + POST_PATH), HttpStatus.SC_CREATED);
            callServerMethod(server, new GetMethod(this.hostUrl + System.getenv("GET_PATH")), HttpStatus.SC_OK);
        });
    }

    private void callServerMethod(final HttpClient client, final HttpMethod method, final int expectedStatus) {
        try {
            Integer statusCode = client.executeMethod(method);
            if (!statusCode.equals(expectedStatus)) {
                System.err.println(method.getClass().getSimpleName() + " failed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

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
    }

    public static void main(final String[] args) {
        if (args.length != 4) {
            System.err.println("Please provide 4 arguments to run the program. (threadCount, groupCount, delayInSeconds, hostUrl)");
        }

        final int threadCount = Integer.parseInt(args[0]);
        final int groupCount = Integer.parseInt(args[1]);
        final int delayInSeconds = Integer.parseInt(args[2]);
        final String hostUrl = args[3];

        Main main = new Main(threadCount, groupCount, hostUrl);
        if (!main.hostUrlExists()) {
            System.err.println("Initial connection to host url: " + hostUrl + " failed.");
            return;
        }

        System.out.println("Initial connection to host url: " + hostUrl + " success");
        System.out.println("Initial run...");
        main.initialRun();
        main.startStopwatch();
        CountDownLatch latch = new CountDownLatch(threadCount * groupCount);
        IntStream.range(0, groupCount).forEach(i -> {
            System.out.println("Processing group: " + i + "...");
            main.processGroup(latch);
            try {
                Thread.sleep(delayInSeconds * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println("Something went wrong in Main.processGroup()");
            throw new RuntimeException(e);
        }
        main.stopStopwatch();
        main.reportStatistics();
    }

    private Boolean hostUrlExists() {
        HttpClient server = new HttpClient();
        HttpMethod testConnectionMethod = new GetMethod(this.hostUrl + System.getenv("GET_PATH"));
        try {
            server.executeMethod(testConnectionMethod);
        } catch (IOException e) {
            return false;
        } finally {
            testConnectionMethod.releaseConnection();
        }
        return true;
    }
}