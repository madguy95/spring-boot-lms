package com.springjwt.module.course.domain.repository;

import com.springjwt.module.course.domain.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"sessions", "discounts"})
    @Query(value = """
            select distinct c from Course c
            where (:category is null or c.category = :category)
              and (:status is null or c.status = :status)
              and (:search is null
                   or lower(cast(c.title as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.code as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.tagline as string)) like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(distinct c.id) from Course c
            where (:category is null or c.category = :category)
              and (:status is null or c.status = :status)
              and (:search is null
                   or lower(cast(c.title as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.code as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.tagline as string)) like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<Course> search(@Param("category") String category,
                        @Param("status") String status,
                        @Param("search") String search,
                        Pageable pageable);

    @EntityGraph(attributePaths = {"sessions", "discounts"})
    Optional<Course> findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countByStatus(String status);

    long countByCategory(String category);
}
