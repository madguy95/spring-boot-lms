package com.springjwt.module.blog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Lean row for the admin blog list grid + the public /blog index. Excludes the
 * full body (which can be several KB of HTML) and the attached-class snapshot
 * (only needed on the detail page).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogPostListItemDto {
    private Long id;
    private String slug;
    private String type;
    private String status;
    private String category;
    private String title;
    private String excerpt;
    private String coverUrl;
    private Integer hue;
    private List<String> tags;
    private Boolean featured;
    private Long classId;
    private AuthorDto author;
    private Instant publishedAt;
    private String publishedLabel;
    private String metaLabel;
    private Integer readingMinutes;
}
