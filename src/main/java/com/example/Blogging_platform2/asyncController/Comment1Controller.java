package com.example.Blogging_platform2.asyncController;

import com.example.Blogging_platform2.asyncService.Comment1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/comments")
public class Comment1Controller {

    @Autowired
    private Comment1Service commentService;

    // Get all comments for a post
    @GetMapping("/post/{postId}")
    public CompletableFuture<List<Map<String, Object>>> getCommentsByPost(@PathVariable Long postId) {
        return commentService.getCommentsByPost(postId);
    }

    // Add a new comment to a post
    @PostMapping("/post/{postId}")
    public CompletableFuture<Map<String, Object>> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> commentData) {
        return commentService.addComment(postId, commentData);
    }
}
