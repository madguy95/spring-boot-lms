package com.springjwt.module.file.domain.service;

import com.springjwt.common.exception.BadRequestException;
import com.springjwt.module.file.domain.entity.FileEntity;
import com.springjwt.module.file.domain.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDomainService {

    private final FileRepository fileRepository;

    @Transactional
    public FileEntity save(FileEntity fileEntity) {
        return fileRepository.save(fileEntity);
    }

    @Transactional(readOnly = true)
    public FileEntity findById(Long id) {
        return fileRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BadRequestException("File not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public FileEntity findByStoredName(String storedName) {
        return fileRepository.findByStoredNameAndIsDeletedFalse(storedName)
                .orElseThrow(() -> new BadRequestException("File not found: " + storedName));
    }

    @Transactional
    public void softDelete(Long id) {
        FileEntity fileEntity = findById(id);
        fileEntity.setIsDeleted(true);
        fileRepository.save(fileEntity);
        log.info("File soft deleted: {}", id);
    }

    @Transactional
    public void hardDelete(Long id) {
        fileRepository.deleteById(id);
        log.info("File hard deleted: {}", id);
    }
}

