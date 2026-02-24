package com.example.Blogging_platform2.asyncController;
import com.example.Blogging_platform2.aspect.MetricsLogger;
import com.example.Blogging_platform2.asyncService.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private MetricsLogger metricsLogger;

    @GetMapping("/posts/{postId}/views")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getPostViews(@PathVariable Long postId) {
        return analyticsService.getPostAnalytics(postId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}/engagement")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getUserEngagement(@PathVariable Long userId) {
        return analyticsService.getUserEngagementMetrics(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/trending")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getTrendingPosts(
            @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getTrendingPosts(limit)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, MetricsLogger.PerformanceMetric>> getPerformanceMetrics() {
        metricsLogger.logMemoryUsage();
        return ResponseEntity.ok(metricsLogger.getAllMetrics());
    }

    @PostMapping("/posts/{postId}/track-view")
    public CompletableFuture<ResponseEntity<String>> trackPostView(
            @PathVariable Long postId,
            @RequestParam(required = false) String userId) {

        return analyticsService.trackPostView(postId, userId)
                .thenApply(success -> {
                    if (success) {
                        return ResponseEntity.ok("View tracked successfully");
                    } else {
                        return ResponseEntity.internalServerError().body("Failed to track view");
                    }
                });
    }
}
