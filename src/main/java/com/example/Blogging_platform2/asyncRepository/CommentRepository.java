// src/main/java/com/example/blogging_platform/asyncRepository/CommentRepository.java
package com.example.blogging_platform.asyncRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class CommentRepository {

    private static final Logger logger = LoggerFactory.getLogger(CommentRepository.class);

    // Thread-safe collections for concurrent access
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Map<String, Object>>> commentsByPost
            = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Async("taskExecutor")
    public CompletableFuture<List<Map<String, Object>>> findCommentsByPostId(Long postId) {
        logger.info("Fetching comments for post: {}", postId);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate database query delay
            try {
                Thread.sleep(50 + random.nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<Map<String, Object>> comments = commentsByPost.getOrDefault(postId, new CopyOnWriteArrayList<>());

            // Sort comments by timestamp (newest first) efficiently
            return comments.parallelStream()
                    .sorted((c1, c2) -> {
                        Long t1 = (Long) c1.getOrDefault("timestamp", 0L);
                        Long t2 = (Long) c2.getOrDefault("timestamp", 0L);
                        return t2.compareTo(t1);
                    })
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        });
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> saveComment(Long postId, Map<String, Object> commentData) {
        logger.info("Saving comment for post: {}", postId);

        return CompletableFuture.supplyAsync(() -> {
            // Simulate processing delay
            try {
                Thread.sleep(25 + random.nextInt(75));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            commentData.put("id", random.nextLong(100000));
            commentData.put("postId", postId);
            commentData.put("timestamp", System.currentTimeMillis());
            commentData.put("status", "approved");

            // Add to thread-safe collection
            commentsByPost.computeIfAbsent(postId, k -> new CopyOnWriteArrayList<>()).add(commentData);

            logger.info("Comment saved successfully for post: {}", postId);
            return commentData;
        });
    }
}
