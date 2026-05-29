package com.springjwt.module.blog.model.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Partial update — every field optional. Null values mean "leave unchanged"
 * rather than "set to null". Use the dedicated DELETE endpoint to remove a post
 * entirely, and the type field cannot be changed after creation (a workshop
 * post cannot become an article and vice versa — it changes the FK contract).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBlogPostRequest {

    @Pattern(regexp = "^(draft|published)$", message = "blog.validation.status.invalid")
    private String status;

    @Size(max = 40, message = "blog.validation.category.size")
    private String category;

    @Size(max = 220, message = "blog.validation.title.size")
    private String title;

    @Size(max = 400, message = "blog.validation.excerpt.size")
    private String excerpt;

    private String body;

    @Size(max = 500, message = "blog.validation.coverUrl.size")
    private String coverUrl;

    @Min(value = 0, message = "blog.validation.hue.min")
    @Max(value = 360, message = "blog.validation.hue.max")
    private Integer hue;

    private List<String> tags;

    private Boolean featured;

    private Long classId;

    @Min(value = 1, message = "blog.validation.readingMinutes.min")
    @Max(value = 120, message = "blog.validation.readingMinutes.max")
    private Integer readingMinutes;
}
