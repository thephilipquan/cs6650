package org.philipquan.report;

import static org.philipquan.RunConfig.ALBUM_ENDPOINT;
import static org.philipquan.RunConfig.REACTION_ENDPOINT;

public class RequestStatistic {
    private final String endpoint;
    private final String requestType;
    private final long latency;
    private final int statusCode;

    public static RequestStatistic createPostAlbum(int statusCode, Timer timer) {
        return new RequestStatistic(ALBUM_ENDPOINT, "POST", statusCode, timer.getElapsedTime());
    }

    public static RequestStatistic createPostReaction(int statusCode, Timer timer) {
        return new RequestStatistic(REACTION_ENDPOINT, "POST", statusCode, timer.getElapsedTime());
    }

    public static RequestStatistic createGetReaction(int statusCode, Timer timer) {
        return new RequestStatistic(REACTION_ENDPOINT, "GET", statusCode, timer.getElapsedTime());
    }

    private RequestStatistic(String endpoint, String requestType, int statusCode, long latency) {
        this.endpoint = endpoint;
        this.requestType = requestType;
        this.statusCode = statusCode;
        this.latency = latency;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public long getLatency() {
        return this.latency;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}