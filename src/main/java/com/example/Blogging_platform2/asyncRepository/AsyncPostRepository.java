package com.example.Blogging_platform2.asyncRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class AsyncPostRepository {

    private static final Logger logger = LoggerFactory.getLogger(AsyncPostRepository.class);

    // Thread-safe collections
    private final ConcurrentHashMap<Long, Map<String, Object>> posts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<String>> comments = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Save a new post
    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> savePost(Map<String, Object> postData) {
        return CompletableFuture.supplyAsync(() -> {
            Long postId = random.nextLong(1000000);
            postData.put("id", postId);
            postData.put("createdAt", System.currentTimeMillis());
            postData.put("status", "published");
            postData.put("views", 0L);

            posts.put(postId, postData);
            comments.put(postId, new CopyOnWriteArrayList<>());

            logger.info("Post saved successfully with ID: {}", postId);
            return postData;
        });
    }

    // Find posts by category
    @Async("taskExecutor")
    public CompletableFuture<List<Map<String, Object>>> findPostsByCategory(String category) {
        return CompletableFuture.supplyAsync(() -> {
            return posts.values().parallelStream()
                    .filter(post -> category.equals(post.get("category")))
                    .sorted((p1, p2) -> {
                        Long views1 = (Long) p1.getOrDefault("views", 0L);
                        Long views2 = (Long) p2.getOrDefault("views", 0L);
                        return views2.compareTo(views1);
                    })
                    .collect(Collectors.toList());
        });
    }

    // Add comment to a post
    @Async("taskExecutor")
    public CompletableFuture<Void> addComment(Long postId, String comment) {
        return CompletableFuture.runAsync(() -> {
            comments.computeIfAbsent(postId, k -> new CopyOnWriteArrayList<>()).add(comment);
            logger.info("Comment added to post {}", postId);
        });
    }

    // Get comments for a post
    public List<String> getComments(Long postId) {
        return comments.getOrDefault(postId, new CopyOnWriteArrayList<>());
    }

    // Find post by ID
    public Optional<Map<String, Object>> findById(Long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    // Delete post
    public void deletePost(Long postId) {
        posts.remove(postId);
        comments.remove(postId);
        logger.info("Post {} deleted successfully", postId);
    }
}
