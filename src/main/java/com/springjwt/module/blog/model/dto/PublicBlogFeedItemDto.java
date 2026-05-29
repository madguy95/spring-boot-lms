package com.springjwt.module.blog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * Item in the mixed Workshop + Blog feed shown on the marketing landing page.
 *
 * <p>The {@code kind} discriminator mirrors the FE {@code HomeFeedItem} type:
 * <ul>
 *   <li>{@code 'workshop'} — {@code attachedClass} is populated; spotlight + tile
 *       cards show date + seats from it.</li>
 *   <li>{@code 'article'} — {@code attachedClass} is null.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicBlogFeedItemDto {
    private String kind; // 'workshop' | 'article'
    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String category;
    private String coverUrl;
    private Integer hue;
    private String metaLabel;
    private Integer readingMinutes;
    private Instant publishedAt;
    private AttachedClassDto attachedClass;
    /**
     * Editorial pin. Featured items bubble to the top of the home feed within
     * each kind (workshops still use upcoming-date as a tiebreaker).
     */
    private Boolean featured;
}
