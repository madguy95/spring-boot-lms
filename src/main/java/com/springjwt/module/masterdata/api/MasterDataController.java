package com.springjwt.module.masterdata.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.masterdata.business.MasterDataService;
import com.springjwt.module.masterdata.model.dto.MasterDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "Generic lookup data (tool, subject, location, room)")
@SecurityRequirement(name = "Bearer Authentication")
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping("/public/master-data")
    @Operation(summary = "List master data items by type")
    public ResponseEntity<ApiResult<List<MasterDataDto>>> listByType(@RequestParam String type) {
        return ResponseFactory.success(masterDataService.listByType(type));
    }
}
