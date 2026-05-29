package com.springjwt.module.blog.model.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBlogPostRequest {

    @NotBlank(message = "blog.validation.type.required")
    @Pattern(regexp = "^(article|workshop)$", message = "blog.validation.type.invalid")
    private String type;

    @NotBlank(message = "blog.validation.status.required")
    @Pattern(regexp = "^(draft|published)$", message = "blog.validation.status.invalid")
    private String status;

    @NotBlank(message = "blog.validation.category.required")
    @Size(max = 40, message = "blog.validation.category.size")
    private String category;

    @NotBlank(message = "blog.validation.title.required")
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

    @Builder.Default
    private List<String> tags = List.of();

    @Builder.Default
    private Boolean featured = false;

    /**
     * Required when {@code type='workshop'} — references the Class this post
     * promotes. Validated in the service layer; the FE picker enforces it before
     * publish.
     */
    private Long classId;

    @Min(value = 1, message = "blog.validation.readingMinutes.min")
    @Max(value = 120, message = "blog.validation.readingMinutes.max")
    private Integer readingMinutes;
}
