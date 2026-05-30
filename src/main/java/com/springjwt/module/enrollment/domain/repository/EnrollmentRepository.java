package com.springjwt.module.enrollment.domain.repository;

import com.springjwt.module.enrollment.domain.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {"requestedCourse", "assignedClass", "assignedClass.course"})
    @Query(value = """
            select e from Enrollment e
            left join e.requestedCourse rc
            left join e.assignedClass ac
            where (:status is null or e.status = :status)
              and (:search is null
                   or lower(cast(e.studentName as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(e.parentName  as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(rc.title      as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(rc.code       as string))   like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(e) from Enrollment e
            left join e.requestedCourse rc
            where (:status is null or e.status = :status)
              and (:search is null
                   or lower(cast(e.studentName as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(e.parentName  as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(rc.title      as string))   like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(rc.code       as string))   like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<Enrollment> search(@Param("status") String status,
                            @Param("search") String search,
                            Pageable pageable);

    @EntityGraph(attributePaths = {"requestedCourse", "assignedClass", "assignedClass.course"})
    Optional<Enrollment> findById(Long id);

    long countByStatus(String status);

    // Roster query for the class detail panel — only "active" rows belong to a
    // class's roster; pending/waitlist/rejected never carry an assigned class.
    @Query("""
            select e from Enrollment e
            where e.assignedClass.id = :classId and e.status = 'active'
            order by e.approvedAt asc, e.id asc
            """)
    List<Enrollment> findRosterByClassId(@Param("classId") Long classId);

    // Admin dashboard: most recent sign-ups across all statuses, joined with
    // course so the FE table can render course title without N+1 fetches.
    @EntityGraph(attributePaths = {"requestedCourse"})
    @Query("""
            select e from Enrollment e
            order by e.submittedAt desc
            """)
    List<Enrollment> findRecentForDashboard(Pageable pageable);

    // Admin dashboard student avatar stack: most recently approved active
    // enrollments. Pageable gives us a cheap top-N without a derived method
    // explosion.
    @Query("""
            select e from Enrollment e
            where e.status = 'active' and e.approvedAt is not null
            order by e.approvedAt desc
            """)
    List<Enrollment> findRecentActive(Pageable pageable);

    // Delta counters: how many active enrollments crossed into active in the
    // window. Mirrors the FE's "+38" chip semantics.
    long countByStatusAndApprovedAtAfter(String status, Instant since);

    long countBySubmittedAtAfter(Instant since);

    // Spark-bar source for the courses KPI: bucketed enrollment count per day.
    // We pull the raw rows in the window and group in Java — small dataset, no
    // DB-specific date functions needed.
    @Query("""
            select e.submittedAt from Enrollment e
            where e.submittedAt >= :since
            """)
    List<Instant> findSubmittedTimestampsSince(@Param("since") Instant since);

    // Pending KPI footer: average seconds a pending row has been waiting. We
    // compute the delta in JPQL as (now - submittedAt) so the DB does the math
    // and the service just formats the result. Returns null when no pending.
    @Query(value = """
            select coalesce(avg(extract(epoch from (now() - e.submitted_at))), 0)
            from enrollments e
            where e.status = 'pending'
            """, nativeQuery = true)
    Double avgPendingWaitSeconds();
}
