package com.springjwt.common.base.request;

import com.springjwt.common.constant.AppConstants;
import com.springjwt.common.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BasePagingRequest {

    @Builder.Default
    @Min(value = 0, message = "validation.page.min")
    private int page = 0;

    @Builder.Default
    @Min(value = 1, message = "validation.size.min")
    @Max(value = AppConstants.MAX_SIZE, message = "validation.size.max") // Thêm max để tránh query quá lớn
    private int size = AppConstants.DEFAULT_SIZE;

    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "validation.sortBy.invalid") // Tránh SQL injection
    private String sortBy;

    @Builder.Default
    private SortDirection sortDirection = SortDirection.ASC;


    public int getOffset() {
        return page * size;
    }

    public boolean isAscending() {
        return this.sortDirection == SortDirection.ASC;
    }
}
