package org.philipquan;

public class MethodStatistic {

    private final long methodStartTime;
    private final String methodType;
    private final long latency;
    private final int statusCode;

    public MethodStatistic(long methodStartTime, String methodType, long latencyInMilliseconds, int statusCode) {
        this.methodStartTime = methodStartTime;
        this.methodType = methodType;
        this.latency = latencyInMilliseconds;
        this.statusCode = statusCode;
    }

    public long getStartRequestTime() {
        return this.methodStartTime;
    }

    public String getMethodType() {
        return this.methodType;
    }

    public long getLatency() {
        return this.latency;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}