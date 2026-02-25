package com.example.Blogging_platform2.asyncService;

import com.example.Blogging_platform2.asyncRepository.Comment1Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class Comment1Service {

    @Autowired
    private Comment1Repository commentRepository;
//
    public CompletableFuture<List<Map<String, Object>>> getCommentsByPost(Long postId) {
        return commentRepository.findCommentsByPostId(postId);
    }

    public CompletableFuture<Map<String, Object>> addComment(Long postId, Map<String, Object> commentData) {
        return commentRepository.saveComment(postId, commentData);
    }
}
