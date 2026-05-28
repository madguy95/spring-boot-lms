package com.springjwt.module.teacher.domain.repository;

import com.springjwt.module.teacher.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

	List<Subject> findByIdIn(Set<Long> ids);
}


