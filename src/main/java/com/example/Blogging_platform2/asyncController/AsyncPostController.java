package com.example.Blogging_platform2.asyncController;

import com.example.Blogging_platform2.asyncService.AsyncPostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/async-posts")
public class AsyncPostController {

    private final AsyncPostService asyncPostService;

    public AsyncPostController(AsyncPostService asyncPostService) {
        this.asyncPostService = asyncPostService;
    }

    @PostMapping
    public CompletableFuture<Map<String, Object>> createPost(
            @RequestBody Map<String, Object> postData) {
        return asyncPostService.processPostCreation(postData);
    }


    @GetMapping
    public CompletableFuture<List<Map<String, Object>>> getPostsByCategory(
            @RequestParam String category) {
        return asyncPostService.getPostsByCategory(category);
    }


    @PostMapping("/{postId}/comments")
    public CompletableFuture<Void> addComment(
            @PathVariable Long postId,
            @RequestBody String comment) {
        return asyncPostService.addComment(postId, comment);
    }


    @GetMapping("/{postId}/comments")
    public List<String> getComments(@PathVariable Long postId) {
        return asyncPostService.getComments(postId);
    }
}
