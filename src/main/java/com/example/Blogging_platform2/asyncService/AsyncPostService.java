package com.example.Blogging_platform2.asyncService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AsyncPostService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPostService.class);

    // Thread-safe collections for concurrent operations
    private final ConcurrentHashMap<Long, Map<String, Object>> posts = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Async("taskExecutor")
    public CompletableFuture<List<Map<String, Object>>> getPostsByCategory(String category) {
        logger.info("Fetching posts for category: {}", category);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate database query
            try {
                Thread.sleep(100 + random.nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Filter and sort posts efficiently
            return posts.values().parallelStream()
                    .filter(post -> category.equals(post.get("category")))
                    .sorted((p1, p2) -> {
                        Long views1 = (Long) p1.getOrDefault("views", 0L);
                        Long views2 = (Long) p2.getOrDefault("views", 0L);
                        return views2.compareTo(views1); // Descending order
                    })
                    .collect(Collectors.toList());
        });
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> processPostCreation(Map<String, Object> postData) {
        logger.info("Processing post creation for: {}", postData.get("title"));

        return CompletableFuture.supplyAsync(() -> {
            // Simulate post processing (validation, content analysis, etc.)
            try {
                Thread.sleep(200 + random.nextInt(300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Long postId = random.nextLong(1000000);
            postData.put("id", postId);
            postData.put("createdAt", System.currentTimeMillis());
            postData.put("status", "published");
            postData.put("views", 0L);

            // Store in thread-safe collection
            posts.put(postId, postData);

            logger.info("Post created successfully with ID: {}", postId);
            return postData;
        });
    }
}
