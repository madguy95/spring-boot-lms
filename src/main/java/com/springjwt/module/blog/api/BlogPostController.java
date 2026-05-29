package com.springjwt.module.blog.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.blog.business.BlogPostService;
import com.springjwt.module.blog.model.dto.BlogPostDto;
import com.springjwt.module.blog.model.dto.BlogPostListItemDto;
import com.springjwt.module.blog.model.dto.PublicBlogFeedItemDto;
import com.springjwt.module.blog.model.request.BlogPostListRequest;
import com.springjwt.module.blog.model.request.CreateBlogPostRequest;
import com.springjwt.module.blog.model.request.UpdateBlogPostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Blog & Workshop endpoints.
 *
 * <p>Routes split into two surfaces:
 * <ul>
 *   <li>{@code /api/blog/**}        — admin (write + read drafts)</li>
 *   <li>{@code /api/public/blog/**} — unauthenticated (published only)</li>
 * </ul>
 *
 * Public routes must be whitelisted in {@code WebSecurityConfig} alongside
 * {@code /api/public/courses/**}.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Blog & Workshop", description = "Editorial posts and workshop attachments")
public class BlogPostController {

    private final BlogPostService blogPostService;

    // ---------------- Admin ----------------

    @GetMapping("/blog")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List blog posts (admin)",
            description = "Admin list — supports filtering by type/status/search and can include drafts.")
    public ResponseEntity<PagedResult<BlogPostListItemDto>> listPosts(@Valid BlogPostListRequest request) {
        Page<BlogPostListItemDto> data = blogPostService.listPosts(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/blog/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get blog post by id (admin)")
    public ResponseEntity<ApiResult<BlogPostDto>> getPost(@PathVariable Long id) {
        return ResponseFactory.success(blogPostService.getPostById(id));
    }

    @PostMapping("/blog")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create blog post")
    public ResponseEntity<ApiResult<BlogPostDto>> createPost(@Valid @RequestBody CreateBlogPostRequest request) {
        BlogPostDto post = blogPostService.createPost(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/blog/" + post.getId());
        return new ResponseEntity<>(ApiResult.success(post), headers, HttpStatus.CREATED);
    }

    @PutMapping("/blog/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update blog post",
            description = "Partial update — null fields leave the existing value alone. Type is immutable.")
    public ResponseEntity<ApiResult<BlogPostDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBlogPostRequest request) {
        return ResponseFactory.success(blogPostService.updatePost(id, request));
    }

    @DeleteMapping("/blog/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete blog post")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Public ----------------

    @GetMapping("/public/blog")
    @Operation(summary = "Public list of published blog posts",
            description = "Excludes drafts. Used by the in-app /blog page when called by guests.")
    public ResponseEntity<PagedResult<BlogPostListItemDto>> listPublicPosts(@Valid BlogPostListRequest request) {
        // Force public callers off the drafts switch regardless of what they passed.
        request.setIncludeDrafts(false);
        Page<BlogPostListItemDto> data = blogPostService.listPosts(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/public/blog/{slug}")
    @Operation(summary = "Public blog post detail",
            description = "Returns 404 for drafts. Workshop posts include attachedClass populated from the canonical Class record.")
    public ResponseEntity<ApiResult<BlogPostDto>> getPublicPost(@PathVariable String slug) {
        return ResponseFactory.success(blogPostService.getPublicPostBySlug(slug));
    }

    @GetMapping("/public/blog/home-feed")
    @Operation(summary = "Mixed Workshop + Blog feed for the marketing landing page",
            description = "Upcoming workshops first (sorted by start time), articles fill remaining slots. Default limit 4.")
    public ResponseEntity<ApiResult<List<PublicBlogFeedItemDto>>> getHomeFeed(
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        return ResponseFactory.success(blogPostService.getHomeFeed(limit));
    }
}
