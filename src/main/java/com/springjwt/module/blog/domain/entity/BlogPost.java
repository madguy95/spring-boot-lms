package com.springjwt.module.blog.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Editorial post shown on the public marketing site and the in-app /blog page.
 *
 * <p>One row covers both kinds of posts:
 * <ul>
 *   <li>{@code type='article'} — standalone content; {@code classId} is null.</li>
 *   <li>{@code type='workshop'} — promotes a Class; {@code classId} references
 *       the canonical class. Workshop logistics (date/time/location/capacity)
 *       are NOT duplicated here.</li>
 * </ul>
 */
@Entity
@Table(name = "blog_posts", indexes = {
        @Index(name = "idx_blog_posts_status",       columnList = "status"),
        @Index(name = "idx_blog_posts_type",         columnList = "type"),
        @Index(name = "idx_blog_posts_class_id",     columnList = "class_id"),
        @Index(name = "idx_blog_posts_published_at", columnList = "published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "title", nullable = false, length = 220)
    private String title;

    @Column(name = "excerpt", length = 400)
    private String excerpt;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "hue", nullable = false)
    @Builder.Default
    private Integer hue = 200;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    /**
     * FK to {@code classes(id)} for workshop posts. Mapped as a plain Long so we
     * don't pull the entire Class graph on every read — the service joins only
     * what the public detail / home-feed need.
     */
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_label", length = 60)
    private String publishedLabel;

    @Column(name = "meta_label", length = 120)
    private String metaLabel;

    @Column(name = "reading_minutes")
    private Integer readingMinutes;
}
