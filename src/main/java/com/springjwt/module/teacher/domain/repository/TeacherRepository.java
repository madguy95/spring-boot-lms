package com.springjwt.module.teacher.domain.repository;

import com.springjwt.module.teacher.domain.entity.Teacher;
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
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @EntityGraph(attributePaths = {"user", "teacherSubjects", "teacherSubjects.subject"})
    @Query(value = """
            select distinct t from Teacher t
            join t.user u
            left join t.teacherSubjects ts
            left join ts.subject s
            where (:status is null or t.status = :status)
              and (:search is null
                   or lower(cast(concat(t.firstName, ' ', t.lastName) as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(u.email as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(s.name as string)) like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(distinct t.id) from Teacher t
            join t.user u
            left join t.teacherSubjects ts
            left join ts.subject s
            where (:status is null or t.status = :status)
              and (:search is null
                   or lower(cast(concat(t.firstName, ' ', t.lastName) as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(u.email as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(s.name as string)) like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<Teacher> search(@Param("status") String status, @Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "teacherSubjects", "teacherSubjects.subject"})
    Optional<Teacher> findById(Long id);

    @EntityGraph(attributePaths = {"user", "teacherSubjects", "teacherSubjects.subject"})
    Optional<Teacher> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Teacher> findByStatusOrderByFirstNameAscLastNameAsc(String status);

    @EntityGraph(attributePaths = {"user"})
    List<Teacher> findAllByOrderByFirstNameAscLastNameAsc();

    long countByStatus(String status);
}

