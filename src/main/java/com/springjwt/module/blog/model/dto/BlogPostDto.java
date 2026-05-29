package com.springjwt.module.blog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Full blog post payload — used by:
 * <ul>
 *   <li>admin list / detail / create / update responses</li>
 *   <li>public detail by slug (with {@code attachedClass} populated for workshops)</li>
 * </ul>
 *
 * <p>{@code attachedClass} is only present when {@code type='workshop'} and the
 * referenced class still exists; if the class was deleted the field is null and
 * the FE renders a "workshop unavailable" banner.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlogPostDto {
    private Long id;
    private String slug;
    private String type;
    private String status;
    private String category;
    private String title;
    private String excerpt;
    private String body;
    private String coverUrl;
    private Integer hue;
    private List<String> tags;
    private Boolean featured;
    private Long classId;
    private AttachedClassDto attachedClass;
    private AuthorDto author;
    private Instant publishedAt;
    private String publishedLabel;
    private String metaLabel;
    private Integer readingMinutes;
    private Instant createdAt;
    private Instant updatedAt;
}
