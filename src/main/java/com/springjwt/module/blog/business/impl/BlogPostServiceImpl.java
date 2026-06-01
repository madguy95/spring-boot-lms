package com.springjwt.module.blog.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.blog.business.BlogPostService;
import com.springjwt.module.blog.domain.entity.BlogPost;
import com.springjwt.module.blog.domain.repository.BlogPostRepository;
import com.springjwt.module.blog.model.dto.AttachedClassDto;
import com.springjwt.module.blog.model.dto.AuthorDto;
import com.springjwt.module.blog.model.dto.BlogPostDto;
import com.springjwt.module.blog.model.dto.BlogPostListItemDto;
import com.springjwt.module.blog.model.dto.PublicBlogFeedItemDto;
import com.springjwt.module.blog.model.request.BlogPostListRequest;
import com.springjwt.module.blog.model.request.CreateBlogPostRequest;
import com.springjwt.module.blog.model.request.UpdateBlogPostRequest;
import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.domain.service.ClassLifecyclePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService {

    private static final String TYPE_ARTICLE = "article";
    private static final String TYPE_WORKSHOP = "workshop";
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";

    // Vietnam timezone — used to compute `startsAt` for workshop classes so the
    // landing-page sort matches the date Vietnamese parents would see.
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BlogPostRepository blogPostRepository;
    private final ClassRepository classRepository;
    private final ClassLifecyclePolicy classLifecyclePolicy;

    // ------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------

    @Override
    public Page<BlogPostListItemDto> listPosts(BlogPostListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        Page<BlogPost> posts = blogPostRepository.search(
                request.getType(),
                request.getStatus(),
                normalize(request.getSearch()),
                request.isIncludeDrafts(),
                pageable);
        List<BlogPostListItemDto> data = posts.stream().map(this::toListItem).toList();
        return new PageImpl<>(data, pageable, posts.getTotalElements());
    }

    @Override
    public BlogPostDto getPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> notFound("blog.post.notFound"));
        return toDto(post, loadAttachedClass(post));
    }

    @Override
    public BlogPostDto getPublicPostBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .filter(p -> STATUS_PUBLISHED.equals(p.getStatus()))
                .orElseThrow(() -> notFound("blog.post.notFound"));
        return toDto(post, loadAttachedClass(post));
    }

    // ------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public BlogPostDto createPost(CreateBlogPostRequest request) {
        validateClassReference(request.getType(), request.getClassId(), null);

        String slug = generateUniqueSlug(request.getTitle());
        Instant now = Instant.now();
        Instant publishedAt = STATUS_PUBLISHED.equals(request.getStatus()) ? now : null;

        BlogPost post = BlogPost.builder()
                .slug(slug)
                .type(request.getType())
                .status(request.getStatus())
                .category(request.getCategory())
                .title(request.getTitle().trim())
                .excerpt(trimToNull(request.getExcerpt()))
                .body(request.getBody())
                .coverUrl(trimToNull(request.getCoverUrl()))
                .hue(request.getHue() != null ? request.getHue() : 200)
                .tags(joinTags(request.getTags()))
                .featured(Boolean.TRUE.equals(request.getFeatured()))
                .classId(TYPE_WORKSHOP.equals(request.getType()) ? request.getClassId() : null)
                .publishedAt(publishedAt)
                .readingMinutes(request.getReadingMinutes())
                .build();

        // Denormalized display strings — built once on write so the list view
        // doesn't recompute Vietnamese locale formatting on every request.
        applyDenormalizedLabels(post, loadClassIfWorkshop(post));

        BlogPost saved = blogPostRepository.save(post);
        return toDto(saved, loadAttachedClass(saved));
    }

    @Override
    @Transactional
    public BlogPostDto updatePost(Long id, UpdateBlogPostRequest request) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> notFound("blog.post.notFound"));

        // Type is immutable. Class FK changes are validated against the post's
        // existing type — workshop posts must keep a non-null classId.
        Long nextClassId = request.getClassId() != null ? request.getClassId() : post.getClassId();
        validateClassReference(post.getType(), nextClassId, post.getId());

        if (request.getTitle() != null) post.setTitle(request.getTitle().trim());
        if (request.getCategory() != null) post.setCategory(request.getCategory());
        if (request.getExcerpt() != null) post.setExcerpt(trimToNull(request.getExcerpt()));
        if (request.getBody() != null) post.setBody(request.getBody());
        if (request.getCoverUrl() != null) post.setCoverUrl(trimToNull(request.getCoverUrl()));
        if (request.getHue() != null) post.setHue(request.getHue());
        if (request.getTags() != null) post.setTags(joinTags(request.getTags()));
        if (request.getFeatured() != null) post.setFeatured(request.getFeatured());
        if (request.getReadingMinutes() != null) post.setReadingMinutes(request.getReadingMinutes());
        if (TYPE_WORKSHOP.equals(post.getType()) && request.getClassId() != null) {
            post.setClassId(request.getClassId());
        }

        // Status flip drives publishedAt: draft -> published stamps now; the
        // reverse leaves the timestamp intact so re-publishing is idempotent.
        if (request.getStatus() != null && !request.getStatus().equals(post.getStatus())) {
            post.setStatus(request.getStatus());
            if (STATUS_PUBLISHED.equals(request.getStatus()) && post.getPublishedAt() == null) {
                post.setPublishedAt(Instant.now());
            }
        }

        applyDenormalizedLabels(post, loadClassIfWorkshop(post));

        BlogPost saved = blogPostRepository.save(post);
        return toDto(saved, loadAttachedClass(saved));
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw notFound("blog.post.notFound");
        }
        blogPostRepository.deleteById(id);
    }

    // ------------------------------------------------------------------
    // Public landing feed
    // ------------------------------------------------------------------

    @Override
    public List<PublicBlogFeedItemDto> getHomeFeed(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 12));
        // Pull a generous window of recent published posts and partition into
        // articles + workshops here. Keeping the partition logic in the service
        // (not SQL) makes the mix-rules easy to evolve without schema changes.
        List<BlogPost> recent = blogPostRepository.findRecentPublished(PageRequest.of(0, safeLimit * 3));

        List<PublicBlogFeedItemDto> workshops = new ArrayList<>();
        List<PublicBlogFeedItemDto> articles = new ArrayList<>();

        for (BlogPost post : recent) {
            if (TYPE_WORKSHOP.equals(post.getType()) && post.getClassId() != null) {
                AttachedClassDto attached = loadAttachedClass(post);
                if (attached == null) continue;
                if ("cancelled".equals(attached.getStatus())) continue;
                workshops.add(toFeedItem(post, attached, "workshop"));
            } else if (TYPE_ARTICLE.equals(post.getType())) {
                articles.add(toFeedItem(post, null, "article"));
            }
        }

        // Sort within each kind by (featured DESC, dateField).
        // Featured items pinned by admins surface first; the date field is the
        // tiebreaker — earliest upcoming start for workshops (urgency wins),
        // most-recent publish for articles (freshness wins).
        Comparator<PublicBlogFeedItemDto> featuredFirst =
                Comparator.comparing(
                        (PublicBlogFeedItemDto x) -> Boolean.TRUE.equals(x.getFeatured()),
                        Comparator.reverseOrder());

        workshops.sort(featuredFirst.thenComparing(
                (PublicBlogFeedItemDto x) -> x.getAttachedClass().getStartsAt(),
                Comparator.nullsLast(Comparator.naturalOrder())));

        articles.sort(featuredFirst.thenComparing(
                PublicBlogFeedItemDto::getPublishedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Reserve one slot for an article whenever both kinds exist — keeps the
        // feed from becoming an all-workshop catalog when many are open.
        int maxWorkshops = !articles.isEmpty()
                ? Math.min(workshops.size(), Math.max(1, safeLimit - 1))
                : Math.min(workshops.size(), safeLimit);

        List<PublicBlogFeedItemDto> result = new ArrayList<>();
        result.addAll(workshops.subList(0, maxWorkshops));
        int remaining = safeLimit - result.size();
        if (remaining > 0 && !articles.isEmpty()) {
            result.addAll(articles.subList(0, Math.min(remaining, articles.size())));
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Mapping helpers
    // ------------------------------------------------------------------

    private BlogPostListItemDto toListItem(BlogPost post) {
        return BlogPostListItemDto.builder()
                .id(post.getId())
                .slug(post.getSlug())
                .type(post.getType())
                .status(post.getStatus())
                .category(post.getCategory())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .coverUrl(post.getCoverUrl())
                .hue(post.getHue())
                .tags(splitTags(post.getTags()))
                .featured(post.getFeatured())
                .classId(post.getClassId())
                .author(buildAuthor(post))
                .publishedAt(post.getPublishedAt())
                .publishedLabel(post.getPublishedLabel())
                .metaLabel(post.getMetaLabel())
                .readingMinutes(post.getReadingMinutes())
                .build();
    }

    private BlogPostDto toDto(BlogPost post, AttachedClassDto attachedClass) {
        return BlogPostDto.builder()
                .id(post.getId())
                .slug(post.getSlug())
                .type(post.getType())
                .status(post.getStatus())
                .category(post.getCategory())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .body(post.getBody())
                .coverUrl(post.getCoverUrl())
                .hue(post.getHue())
                .tags(splitTags(post.getTags()))
                .featured(post.getFeatured())
                .classId(post.getClassId())
                .attachedClass(attachedClass)
                .author(buildAuthor(post))
                .publishedAt(post.getPublishedAt())
                .publishedLabel(post.getPublishedLabel())
                .metaLabel(post.getMetaLabel())
                .readingMinutes(post.getReadingMinutes())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private PublicBlogFeedItemDto toFeedItem(BlogPost post, AttachedClassDto attached, String kind) {
        return PublicBlogFeedItemDto.builder()
                .kind(kind)
                .id(post.getId())
                .slug(post.getSlug())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .category(post.getCategory())
                .coverUrl(post.getCoverUrl())
                .hue(post.getHue())
                .metaLabel(post.getMetaLabel())
                .readingMinutes(post.getReadingMinutes())
                .publishedAt(post.getPublishedAt())
                .attachedClass(attached)
                .featured(Boolean.TRUE.equals(post.getFeatured()))
                .build();
    }

    private AuthorDto buildAuthor(BlogPost post) {
        // Until a first-class Author exists, surface the audit user as the
        // visible author. The FE only needs name + initials + role chip.
        String name = optional(post.getCreatedBy());
        return AuthorDto.builder()
                .name(name)
                .initials(deriveInitials(name))
                .role("admin")
                .build();
    }

    private AttachedClassDto loadAttachedClass(BlogPost post) {
        if (!TYPE_WORKSHOP.equals(post.getType()) || post.getClassId() == null) return null;
        return classRepository.findById(post.getClassId())
                .map(this::buildAttachedClass)
                .orElse(null);
    }

    private ClassEntity loadClassIfWorkshop(BlogPost post) {
        if (!TYPE_WORKSHOP.equals(post.getType()) || post.getClassId() == null) return null;
        return classRepository.findById(post.getClassId()).orElse(null);
    }

    private AttachedClassDto buildAttachedClass(ClassEntity cls) {
        Instant startsAt = computeStartsAt(cls);
        return AttachedClassDto.builder()
                .id(cls.getId())
                .courseId(cls.getCourse() != null ? cls.getCourse().getId() : null)
                .courseCode(cls.getCourse() != null ? cls.getCourse().getCode() : null)
                .courseTitle(cls.getCourse() != null ? cls.getCourse().getTitle() : null)
                .startDate(cls.getStartDate())
                .endDate(cls.getEndDate())
                .schedule(cls.getSchedule())
                .location(cls.getLocation())
                .room(cls.getRoom())
                .minAge(cls.getCourse() != null ? cls.getCourse().getMinAge() : null)
                .maxAge(cls.getCourse() != null ? cls.getCourse().getMaxAge() : null)
                .capacity(cls.getCapacity())
                .enrolled(cls.getEnrolled())
                .tuitionAmount(cls.getCourse() != null ? cls.getCourse().getTuitionAmount() : null)
                .status(classLifecyclePolicy.deriveDisplayStatus(cls, LocalDate.now(VN_ZONE)))
                .startsAt(startsAt)
                .build();
    }

    private Instant computeStartsAt(ClassEntity cls) {
        LocalDate startDate = cls.getStartDate();
        if (startDate == null) return null;
        // Prefer the first day_schedule's startTime when available; otherwise
        // assume the class begins at start-of-day in VN time. startTime is
        // stored as a "HH:mm" string so we parse defensively.
        return cls.getDaySchedules().stream()
                .min(Comparator.comparing(ClassDaySchedule::getPosition))
                .map(ds -> parseTime(ds.getStartTime())
                        .map(t -> LocalDateTime.of(startDate, t).atZone(VN_ZONE).toInstant())
                        .orElse(null))
                .orElseGet(() -> startDate.atStartOfDay(VN_ZONE).toInstant());
    }

    private static Optional<LocalTime> parseTime(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(LocalTime.parse(s.trim()));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------
    // Field plumbing
    // ------------------------------------------------------------------

    private void applyDenormalizedLabels(BlogPost post, ClassEntity attachedClass) {
        // Published label = "01 Th6 2026" — used by list view; recomputed when
        // publishedAt changes. We use Vietnamese month abbreviation so we don't
        // need a locale-aware formatter at read-time.
        Instant ts = post.getPublishedAt() != null ? post.getPublishedAt() : Instant.now();
        post.setPublishedLabel(formatVietnameseDate(ts));

        if (TYPE_WORKSHOP.equals(post.getType()) && attachedClass != null) {
            // metaLabel format: "DD Th{M} YYYY · {location}" — matches the FE
            // BlogCard layout.
            String dateLabel = attachedClass.getStartDate() != null
                    ? formatVietnameseDate(attachedClass.getStartDate().atStartOfDay(VN_ZONE).toInstant())
                    : "";
            post.setMetaLabel(dateLabel + " · " + optional(attachedClass.getLocation()));
        } else {
            if (post.getReadingMinutes() != null) {
                post.setMetaLabel(post.getPublishedLabel() + " · " + post.getReadingMinutes() + " phút đọc");
            } else {
                post.setMetaLabel(post.getPublishedLabel());
            }
        }
    }

    private void validateClassReference(String type, Long classId, Long currentPostId) {
        if (TYPE_WORKSHOP.equals(type)) {
            if (classId == null) {
                throw new AppException("Workshop post requires a classId", HttpStatus.BAD_REQUEST);
            }
            classRepository.findById(classId)
                    .orElseThrow(() -> new AppException("Class not found: " + classId, HttpStatus.BAD_REQUEST));
        } else if (TYPE_ARTICLE.equals(type) && classId != null) {
            throw new AppException("Article post must not reference a class", HttpStatus.BAD_REQUEST);
        }
        // currentPostId reserved for future uniqueness checks (e.g. one post per class).
    }

    /**
     * Convert a title into a URL slug, append -2/-3/... if collides. Slug stays
     * stable across updates so published URLs don't break — recompute only on
     * create.
     */
    private String generateUniqueSlug(String title) {
        String base = slugify(title);
        if (base.isEmpty()) base = "post";
        String candidate = base;
        int suffix = 2;
        while (blogPostRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
            // Defensive cap — astronomically unlikely to hit.
            if (suffix > 9999) {
                throw new AppException("Could not generate a unique slug from title", HttpStatus.CONFLICT);
            }
        }
        return candidate;
    }

    // ------------------------------------------------------------------
    // String utilities
    // ------------------------------------------------------------------

    private static String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized;
    }

    private static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return String.join(",", tags.stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalize(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String optional(String s) {
        return s == null ? "" : s;
    }

    private static String deriveInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private static final String[] VN_MONTHS = {
            "Th1", "Th2", "Th3", "Th4", "Th5", "Th6",
            "Th7", "Th8", "Th9", "Th10", "Th11", "Th12"
    };

    private static String formatVietnameseDate(Instant ts) {
        LocalDate d = ts.atZone(VN_ZONE).toLocalDate();
        return String.format("%02d %s %d", d.getDayOfMonth(), VN_MONTHS[d.getMonthValue() - 1], d.getYear());
    }

    private static Sort buildSort(String sortBy, String sortDirection) {
        String field = Optional.ofNullable(sortBy).filter(s -> !s.isBlank()).orElse("publishedAt");
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private static AppException notFound(String messageKey) {
        return new AppException("Blog post not found", HttpStatus.NOT_FOUND);
    }
}
