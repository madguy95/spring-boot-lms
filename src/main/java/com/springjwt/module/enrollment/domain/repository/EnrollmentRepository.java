package com.springjwt.module.enrollment.domain.repository;

import com.springjwt.module.enrollment.domain.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
