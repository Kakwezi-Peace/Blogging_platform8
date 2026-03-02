package com.example.Blogging_platform2.asyncService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class AsyncPostService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPostService.class);
     // a) concurrentHashMap
    // Thread-safe collections using concurrentHashMap
    private final ConcurrentHashMap<Long, Map<String, Object>> posts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<String>> comments = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Fetch posts by category asynchronously
    @Async("taskExecutor")
    public CompletableFuture<List<Map<String, Object>>> getPostsByCategory(String category) {
        logger.info("Fetching posts for category: {}", category);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100 + random.nextInt(100)); // simulate DB query
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return posts.values().parallelStream()
                    .filter(post -> category.equals(post.get("category")))
                    .sorted((p1, p2) -> {
                        Long views1 = (Long) p1.getOrDefault("views", 0L);
                        Long views2 = (Long) p2.getOrDefault("views", 0L);
                        return views2.compareTo(views1); // sort descending by views
                    })
                    .collect(Collectors.toList());
        });
    }

    // Process post creation asynchronously
    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> processPostCreation(Map<String, Object> postData) {
        logger.info("Processing post creation for: {}", postData.get("title"));

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(200 + random.nextInt(300)); // simulate processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Long postId = random.nextLong(1000000);
            postData.put("id", postId);
            postData.put("createdAt", System.currentTimeMillis());
            postData.put("status", "published");
            postData.put("views", 0L);

            posts.put(postId, postData);
            comments.put(postId, new CopyOnWriteArrayList<>());

            logger.info("Post created successfully with ID: {}", postId);
            return postData;
        });
    }
   //b) CopyOnWriteArrayList: Implement thread safety
    // Add comment safely with CopyOnWriteArrayList
    @Async("taskExecutor")
    public CompletableFuture<Void> addComment(Long postId, String comment) {
        return CompletableFuture.runAsync(() -> {
            comments.computeIfAbsent(postId, k -> new CopyOnWriteArrayList<>()).add(comment);
            logger.info("Comment added to post {}", postId);
        });
    }
    // Efficient for read-heavy loads since it is concurrentHashMap
    public List<String> getComments(Long postId) {
        return comments.getOrDefault(postId, new CopyOnWriteArrayList<>());
    }
}
