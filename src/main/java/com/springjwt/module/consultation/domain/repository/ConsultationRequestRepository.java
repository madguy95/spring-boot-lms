package com.springjwt.module.consultation.domain.repository;

import com.springjwt.module.consultation.domain.entity.ConsultationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

    @EntityGraph(attributePaths = {"interestedCourse"})
    @Query(value = """
            select c from ConsultationRequest c
            left join c.interestedCourse ic
            where (:status is null or c.status = :status)
              and (:search is null
                   or lower(cast(c.parentName  as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.parentPhone as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.childName   as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(ic.title      as string)) like lower(cast(concat('%', :search, '%') as string)))
            """,
            countQuery = """
            select count(c) from ConsultationRequest c
            left join c.interestedCourse ic
            where (:status is null or c.status = :status)
              and (:search is null
                   or lower(cast(c.parentName  as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.parentPhone as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(c.childName   as string)) like lower(cast(concat('%', :search, '%') as string))
                   or lower(cast(ic.title      as string)) like lower(cast(concat('%', :search, '%') as string)))
            """)
    Page<ConsultationRequest> search(@Param("status") String status,
                                     @Param("search") String search,
                                     Pageable pageable);

    @EntityGraph(attributePaths = {"interestedCourse"})
    Optional<ConsultationRequest> findById(Long id);

    long countByStatus(String status);
}
