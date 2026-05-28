package com.springjwt.module.masterdata.business;

import com.springjwt.module.masterdata.model.dto.MasterDataDto;

import java.util.List;

public interface MasterDataService {

    List<MasterDataDto> listByType(String type);

    void validateCodeExists(String type, String code);
}
