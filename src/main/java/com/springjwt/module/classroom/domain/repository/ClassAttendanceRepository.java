package com.springjwt.module.classroom.domain.repository;

import com.springjwt.module.classroom.domain.entity.ClassAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassAttendanceRepository extends JpaRepository<ClassAttendance, Long> {

    List<ClassAttendance> findBySessionId(Long sessionId);

    List<ClassAttendance> findBySessionIdIn(List<Long> sessionIds);
}
