package com.springjwt.module.masterdata.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.masterdata.business.MasterDataService;
import com.springjwt.module.masterdata.domain.entity.MasterData;
import com.springjwt.module.masterdata.domain.repository.MasterDataRepository;
import com.springjwt.module.masterdata.model.dto.MasterDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl implements MasterDataService {

    private final MasterDataRepository masterDataRepository;

    @Override
    public List<MasterDataDto> listByType(String type) {
        return masterDataRepository.findByTypeAndActiveTrueOrderByPositionAsc(type).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void validateCodeExists(String type, String code) {
        MasterData item = masterDataRepository.findByTypeAndCode(type, code)
                .orElseThrow(() -> new AppException("masterData.code.invalid", HttpStatus.BAD_REQUEST));
        if (Boolean.FALSE.equals(item.getActive())) {
            throw new AppException("masterData.code.inactive", HttpStatus.BAD_REQUEST);
        }
    }

    private MasterDataDto toDto(MasterData entity) {
        return MasterDataDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .position(entity.getPosition())
                .active(entity.getActive())
                .metadata(entity.getMetadata())
                .build();
    }
}
