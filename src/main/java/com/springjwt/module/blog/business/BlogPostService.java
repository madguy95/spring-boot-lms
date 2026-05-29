package com.springjwt.module.blog.business;

import com.springjwt.module.blog.model.dto.BlogPostDto;
import com.springjwt.module.blog.model.dto.BlogPostListItemDto;
import com.springjwt.module.blog.model.dto.PublicBlogFeedItemDto;
import com.springjwt.module.blog.model.request.BlogPostListRequest;
import com.springjwt.module.blog.model.request.CreateBlogPostRequest;
import com.springjwt.module.blog.model.request.UpdateBlogPostRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BlogPostService {

    /**
     * Admin/public list. The {@code includeDrafts} flag inside the request is
     * respected only when the caller is admin; the controller is responsible
     * for forcing it to false on public endpoints.
     */
    Page<BlogPostListItemDto> listPosts(BlogPostListRequest request);

    /**
     * Detail by id (admin path). Includes the joined {@link com.springjwt.module.blog.model.dto.AttachedClassDto}
     * when {@code type='workshop'}.
     */
    BlogPostDto getPostById(Long id);

    /**
     * Detail by slug (public path). Returns 404 for drafts; published posts only.
     */
    BlogPostDto getPublicPostBySlug(String slug);

    BlogPostDto createPost(CreateBlogPostRequest request);

    BlogPostDto updatePost(Long id, UpdateBlogPostRequest request);

    void deletePost(Long id);

    /**
     * Mixed Workshop + Blog feed for the marketing landing page. Same shape +
     * sort as the FE mock implementation: upcoming workshops first, then latest
     * articles, with at least one article slot reserved when both kinds exist.
     */
    List<PublicBlogFeedItemDto> getHomeFeed(int limit);
}
