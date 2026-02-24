package com.example.Blogging_platform2.service;

import com.example.Blogging_platform2.dao.PostDao;
import com.example.Blogging_platform2.dto.PostDto;
import com.example.Blogging_platform2.exception.PostNotFoundException;
import com.example.Blogging_platform2.model.Post;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostDao postDao;

    public PostService(PostDao postDao) {
        this.postDao = postDao;
    }

    // -------------------- Create (REST, DTO-based) --------------------
    @Transactional
    @CachePut(value = "posts", key = "#result.id")
    public PostDto savePost(@Valid PostDto dto) {
        Post post = convertToEntity(dto);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        Post saved = postDao.save(post);
        return convertToDto(saved);
    }

    // -------------------- Create (GraphQL, entity-based) --------------------
    @Transactional
    @CachePut(value = "posts", key = "#result.id")
    public Post savePost(@Valid Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postDao.save(post);
    }

    // -------------------- Read --------------------
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#id")
    public Optional<PostDto> getPostDtoById(Long id) {
        return postDao.findById(id).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "{#page, #size, #sortBy}")
    public Page<PostDto> getAllPosts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return postDao.findAll(pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#keyword")
    public Page<PostDto> searchPostsByTitle(String keyword, Pageable pageable) {
        return postDao.findByTitleContainingIgnoreCase(keyword, pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "'all'")
    public List<PostDto> getAllPostsDto() {
        return postDao.findAll().stream().map(this::convertToDto).toList();
    }

    // -------------------- Update (REST, DTO-based) --------------------
    @Transactional
    @CachePut(value = "posts", key = "#id")
    public PostDto updatePost(Long id, @Valid PostDto updatedPost) {
        Post existingPost = postDao.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found"));
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setUpdatedAt(LocalDateTime.now());
        Post saved = postDao.save(existingPost);
        return convertToDto(saved);
    }

    // -------------------- Update (GraphQL, entity-based) --------------------
    @Transactional
    @CachePut(value = "posts", key = "#id")
    public Post updatePost(Long id, @Valid Post updatedPost) {
        Post existingPost = getPostById(id);
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setUser(updatedPost.getUser());
        existingPost.setUpdatedAt(LocalDateTime.now());
        return postDao.save(existingPost);
    }

    // -------------------- Delete --------------------
    @Transactional
    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(Long id) {
        if (!postDao.existsById(id)) {
            throw new PostNotFoundException("Post not found with id: " + id);
        }
        postDao.deleteById(id);
    }

    // -------------------- Helpers --------------------
    private PostDto convertToDto(Post post) {
        Long userId = post.getUser() != null ? post.getUser().getId() : null;
        return new PostDto(
                post.getId(),
                userId,
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private Post convertToEntity(PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return post;
    }

    // Entity-based method for GraphQL
    public Post getPostById(Long id) {
        return postDao.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Post with ID " + id + " not found"));
    }

    public List<Post> getAllPosts() {
        return postDao.findAll();
    }
}
