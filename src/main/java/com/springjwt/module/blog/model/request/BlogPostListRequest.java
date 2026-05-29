package com.springjwt.module.blog.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostListRequest {

    @Builder.Default
    @Min(value = 1, message = "blog.validation.page.min")
    private int page = 1;

    @Builder.Default
    @Min(value = 1, message = "blog.validation.size.min")
    @Max(value = 100, message = "blog.validation.size.max")
    private int size = 20;

    @Pattern(regexp = "^(article|workshop)$", message = "blog.validation.type.invalid")
    private String type;

    @Pattern(regexp = "^(draft|published)$", message = "blog.validation.status.invalid")
    private String status;

    private String search;

    /**
     * When true, drafts are returned alongside published rows. Only the admin
     * endpoints set this flag; public reads ignore it.
     */
    @Builder.Default
    private boolean includeDrafts = false;

    @Builder.Default
    private String sortBy = "publishedAt";

    @Builder.Default
    @Pattern(regexp = "(?i)^(asc|desc)$", message = "blog.validation.sortDirection.invalid")
    private String sortDirection = "desc";
}
