package com.springjwt.module.blog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Lightweight author snapshot embedded in blog responses. Until a first-class
 * Author entity exists, this is derived from the user/teacher who created the
 * post via the BaseEntity audit columns.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorDto {
    private Long id;
    private String name;
    private String initials;
    private String role; // 'admin' | 'teacher'
}
