package org.philipquan.utility;

public class RequestStatistic {
    private final String requestType;
    private final long latency;
    private final int statusCode;

    public static RequestStatistic createGet(int statusCode, Timer timer) {
        return new RequestStatistic("GET", statusCode, timer.getElapsedTime());
    }

    public static RequestStatistic createPost(int statusCode, Timer timer) {
        return new RequestStatistic("POST", statusCode, timer.getElapsedTime());
    }

    private RequestStatistic(String requestType, int statusCode, long latency) {
        this.requestType = requestType;
        this.statusCode = statusCode;
        this.latency = latency;
    }

    public String getRequestType() {
        return requestType;
    }

    public long getLatency() {
        return latency;
    }

    public int getStatusCode() {
        return statusCode;
    }

}