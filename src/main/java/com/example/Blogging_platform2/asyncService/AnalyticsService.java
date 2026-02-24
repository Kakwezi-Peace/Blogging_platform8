package com.example.Blogging_platform2.asyncService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    // Thread-safe collections for concurrent access
    private final ConcurrentHashMap<Long, AtomicLong> postViews = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicLong> userEngagement = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> getPostAnalytics(Long postId) {
        logger.info("Fetching analytics for post: {}", postId);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate database query delay
            try {
                Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("postId", postId);
            analytics.put("views", postViews.getOrDefault(postId, new AtomicLong(0)).get());
            analytics.put("likes", random.nextInt(100));
            analytics.put("shares", random.nextInt(50));
            analytics.put("comments", random.nextInt(25));

            logger.info("Analytics fetched for post: {} - Views: {}", postId, analytics.get("views"));
            return analytics;
        });
    }

    // another thread pool
    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> getUserEngagementMetrics(Long userId) {
        logger.info("Fetching engagement metrics for user: {}", userId);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate complex calculation
            try {
                Thread.sleep(150 + random.nextInt(100)); // 150-250ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Map<String, Object> engagement = new HashMap<>();
            engagement.put("userId", userId);
            engagement.put("totalPosts", random.nextInt(50) + 1);
            engagement.put("totalViews", userEngagement.getOrDefault(userId, new AtomicLong(0)).get());
            engagement.put("avgViewsPerPost", random.nextInt(200) + 50);
            engagement.put("engagementRate", random.nextDouble() * 10);

            return engagement;
        });
    }

    @Async("analyticsExecutor")
    public CompletableFuture<Map<String, Object>> getTrendingPosts(int limit) {
        logger.info("Fetching trending posts with limit: {}", limit);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate complex trending algorithm
            try {
                Thread.sleep(200 + random.nextInt(150)); // 200-350ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Map<String, Object> trending = new HashMap<>();
            trending.put("limit", limit);
            trending.put("totalTrendingPosts", Math.min(limit, 20));
            trending.put("algorithmVersion", "v2.1");
            trending.put("generatedAt", System.currentTimeMillis());

            return trending;
        });
    }

    @Async("analyticsExecutor")
    public CompletableFuture<Boolean> trackPostView(Long postId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Increment post views atomically
                postViews.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();

                // Track user engagement if userId provided
                if (userId != null && !userId.isEmpty()) {
                    Long userIdLong = Long.parseLong(userId);
                    userEngagement.computeIfAbsent(userIdLong, k -> new AtomicLong(0)).incrementAndGet();
                }

                logger.info("View tracked for post: {} by user: {}", postId, userId);
                return true;
            } catch (Exception e) {
                logger.error("Error tracking view for post: {} by user: {}", postId, userId, e);
                return false;
            }
        });
    }
}
