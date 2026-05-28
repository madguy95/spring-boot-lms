package com.springjwt.module.masterdata.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.common.util.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@Table(name = "master_data",
        uniqueConstraints = @UniqueConstraint(name = "uq_master_data_type_code", columnNames = {"type", "code"}),
        indexes = @Index(name = "idx_master_data_type_active_position", columnList = "type, active, position"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "position", nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private Map<String, Object> metadata;
}
