package com.springjwt.module.classroom.domain.repository;

import com.springjwt.module.classroom.domain.entity.ClassSessionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassSessionNoteRepository extends JpaRepository<ClassSessionNote, Long> {

    List<ClassSessionNote> findBySessionId(Long sessionId);
}
