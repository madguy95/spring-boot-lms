package com.springjwt.module.file.domain.repository;

import com.springjwt.module.file.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByStoredNameAndIsDeletedFalse(String storedName);

    Optional<FileEntity> findByIdAndIsDeletedFalse(Long id);
}

