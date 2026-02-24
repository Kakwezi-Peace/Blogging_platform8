package com.example.Blogging_platform2.aspect;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsLogger {

    private static final Logger logger = LoggerFactory.getLogger(MetricsLogger.class);

    private final ConcurrentHashMap<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();

    public void logPerformanceMetrics(String methodName, long executionTime, boolean success) {
        metrics.compute(methodName, (key, metric) -> {
            if (metric == null) {
                metric = new PerformanceMetric(methodName);
            }
            metric.addExecution(executionTime, success);
            return metric;
        });

        logger.info("Performance Metric - Method: {}, Execution Time: {}ms, Success: {}",
                methodName, executionTime, success);
    }

    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        logger.info("Memory Usage - Total: {}MB, Used: {}MB, Free: {}MB",
                totalMemory / (1024 * 1024),
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024));
    }

    public ConcurrentHashMap<String, PerformanceMetric> getAllMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }

    public static class PerformanceMetric {
        // Getters
        @Getter
        private final String methodName;
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong successfulExecutions = new AtomicLong(0);
        private volatile long minExecutionTime = Long.MAX_VALUE;
        private volatile long maxExecutionTime = Long.MIN_VALUE;

        public PerformanceMetric(String methodName) {
            this.methodName = methodName;
        }

        public void addExecution(long executionTime, boolean success) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);

            if (success) {
                successfulExecutions.incrementAndGet();
            }

            if (executionTime < minExecutionTime) {
                minExecutionTime = executionTime;
            }
            if (executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }
        }

        public long getTotalExecutions() { return totalExecutions.get(); }
        public long getAverageExecutionTime() {
            return totalExecutions.get() > 0 ? totalExecutionTime.get() / totalExecutions.get() : 0;
        }
        public long getMinExecutionTime() { return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime == Long.MIN_VALUE ? 0 : maxExecutionTime; }
        public double getSuccessRate() {
            return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() * 100 : 0;
        }
    }
}
