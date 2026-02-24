package com.example.Blogging_platform2.controller;

import com.example.Blogging_platform2.dto.ApiResponse;
import com.example.Blogging_platform2.dto.PostDto;
import com.example.Blogging_platform2.exception.PostNotFoundException;
import com.example.Blogging_platform2.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post Management", description = "APIs for managing blog posts (create, read, update, delete, search)")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get all posts with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {

        Page<PostDto> postDtos = postService.getAllPosts(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Retrieved posts", postDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(@PathVariable Long id) {
        PostDto postDto = postService.getPostDtoById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found"));
        return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", postDto));
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<PostDto>> createPost(@Valid @RequestBody PostDto postDto) {
        PostDto createdPost = postService.savePost(postDto);
        return new ResponseEntity<>(
                ApiResponse.success("Post created successfully", createdPost),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(@PathVariable Long id,
                                                           @Valid @RequestBody PostDto postDto) {
        PostDto updatedPost = postService.updatePost(id, postDto);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", updatedPost));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts by title keyword")
    public ResponseEntity<ApiResponse<Page<PostDto>>> searchPosts(@RequestParam String query,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> postDtos = postService.searchPostsByTitle(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Found " + postDtos.getTotalElements() + " posts", postDtos));
    }
}
