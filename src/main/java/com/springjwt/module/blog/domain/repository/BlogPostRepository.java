package com.springjwt.module.blog.domain.repository;

import com.springjwt.module.blog.domain.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /**
     * Generic search used by the admin /api/blog list. All filters are nullable
     * — passing {@code null} for a field disables that filter. The boolean
     * {@code includeDrafts} flag is needed in addition to {@code status} so the
     * default landing tab (status=null) can still hide drafts for non-admin
     * call sites if we ever expose this query publicly.
     */
    @Query(value = """
            select p from BlogPost p
            where (:type is null or p.type = :type)
              and (:status is null or p.status = :status)
              and (:includeDrafts = true or p.status <> 'draft')
              and (:search is null
                   or lower(cast(p.title as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(p.excerpt as string))  like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(p.category as string)) like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(p) from BlogPost p
            where (:type is null or p.type = :type)
              and (:status is null or p.status = :status)
              and (:includeDrafts = true or p.status <> 'draft')
              and (:search is null
                   or lower(cast(p.title as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(p.excerpt as string))  like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(p.category as string)) like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<BlogPost> search(@Param("type") String type,
                          @Param("status") String status,
                          @Param("search") String search,
                          @Param("includeDrafts") boolean includeDrafts,
                          Pageable pageable);

    /**
     * Published-only landing-page feed. Ordering by published_at desc; the
     * service layer mixes article + workshop slots downstream so the BE just
     * returns the recent fundamentals here.
     */
    @Query("""
            select p from BlogPost p
            where p.status = 'published'
            order by p.publishedAt desc
            """)
    List<BlogPost> findRecentPublished(Pageable pageable);

    /**
     * Featured posts pinned by admins for the carousel. Limited to published.
     */
    @Query("""
            select p from BlogPost p
            where p.status = 'published' and p.featured = true
            order by p.publishedAt desc
            """)
    List<BlogPost> findFeaturedPublished();
}
