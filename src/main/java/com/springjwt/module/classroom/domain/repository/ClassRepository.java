package com.springjwt.module.classroom.domain.repository;

import com.springjwt.module.classroom.domain.entity.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    // Filter by derived display status — UI tabs surface (open/full/ongoing/
    // completed/draft/unpublished/cancelled). The `today` parameter is passed
    // in (not JPQL CURRENT_DATE) so the service layer controls timezone and
    // makes the query deterministic for tests.
    @EntityGraph(attributePaths = {"course", "teacher", "teacher.user", "daySchedules"})
    @Query(value = """
            select distinct c from ClassEntity c
            left join c.course co
            left join c.teacher t
            left join t.user u
            where (:displayStatus is null
                   or (:displayStatus = 'draft'        and c.lifecycleStatus = 'draft')
                   or (:displayStatus = 'unpublished'  and c.lifecycleStatus = 'unpublished')
                   or (:displayStatus = 'cancelled'    and c.lifecycleStatus = 'cancelled')
                   or (:displayStatus = 'completed'    and c.lifecycleStatus = 'published' and :today > c.endDate)
                   or (:displayStatus = 'ongoing'      and c.lifecycleStatus = 'published' and :today >= c.startDate and :today <= c.endDate)
                   or (:displayStatus = 'full'         and c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled >= c.capacity)
                   or (:displayStatus = 'open'         and c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled < c.capacity))
              and (:search is null
                   or lower(cast(c.name as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(co.title as string))  like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(co.code as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(concat(t.firstName, ' ', t.lastName) as string)) like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(distinct c.id) from ClassEntity c
            left join c.course co
            left join c.teacher t
            where (:displayStatus is null
                   or (:displayStatus = 'draft'        and c.lifecycleStatus = 'draft')
                   or (:displayStatus = 'unpublished'  and c.lifecycleStatus = 'unpublished')
                   or (:displayStatus = 'cancelled'    and c.lifecycleStatus = 'cancelled')
                   or (:displayStatus = 'completed'    and c.lifecycleStatus = 'published' and :today > c.endDate)
                   or (:displayStatus = 'ongoing'      and c.lifecycleStatus = 'published' and :today >= c.startDate and :today <= c.endDate)
                   or (:displayStatus = 'full'         and c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled >= c.capacity)
                   or (:displayStatus = 'open'         and c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled < c.capacity))
              and (:search is null
                   or lower(cast(c.name as string))    like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(co.title as string))  like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(co.code as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(concat(t.firstName, ' ', t.lastName) as string)) like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<ClassEntity> search(@Param("displayStatus") String displayStatus,
                             @Param("search") String search,
                             @Param("today") LocalDate today,
                             Pageable pageable);

    @EntityGraph(attributePaths = {"course", "teacher", "teacher.user", "daySchedules"})
    Optional<ClassEntity> findById(Long id);

    long countByLifecycleStatus(String lifecycleStatus);

    @Query("select count(c) from ClassEntity c where c.lifecycleStatus = 'published' and :today > c.endDate")
    long countCompleted(@Param("today") LocalDate today);

    @Query("select count(c) from ClassEntity c where c.lifecycleStatus = 'published' and :today >= c.startDate and :today <= c.endDate")
    long countOngoing(@Param("today") LocalDate today);

    @Query("select count(c) from ClassEntity c where c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled >= c.capacity")
    long countFull(@Param("today") LocalDate today);

    @Query("select count(c) from ClassEntity c where c.lifecycleStatus = 'published' and :today < c.startDate and c.enrolled < c.capacity")
    long countOpen(@Param("today") LocalDate today);
}
