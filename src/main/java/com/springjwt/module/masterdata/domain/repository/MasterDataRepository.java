package com.springjwt.module.masterdata.domain.repository;

import com.springjwt.module.masterdata.domain.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {

    List<MasterData> findByTypeAndActiveTrueOrderByPositionAsc(String type);

    Optional<MasterData> findByTypeAndCode(String type, String code);
}
