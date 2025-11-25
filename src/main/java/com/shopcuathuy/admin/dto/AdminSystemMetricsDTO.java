package com.shopcuathuy.admin.dto;

import java.time.Instant;

public class AdminSystemMetricsDTO {
    private Instant startedAt;
    private long requestCount;
    private long errorCount;
    private long avgResponseMs;

    public AdminSystemMetricsDTO() {
    }

    public AdminSystemMetricsDTO(Instant startedAt, long requestCount, long errorCount, long avgResponseMs) {
        this.startedAt = startedAt;
        this.requestCount = requestCount;
        this.errorCount = errorCount;
        this.avgResponseMs = avgResponseMs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getAvgResponseMs() {
        return avgResponseMs;
    }

    public void setAvgResponseMs(long avgResponseMs) {
        this.avgResponseMs = avgResponseMs;
    }
}




