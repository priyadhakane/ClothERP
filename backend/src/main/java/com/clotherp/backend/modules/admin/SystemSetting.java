package com.clotherp.backend.modules.admin;

import com.clotherp.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String key;

    @Column(nullable = false, columnDefinition = "text")
    private String value;

    private String description;

    @Column(name = "is_public")
    @Builder.Default
    private boolean publicAccess = false;
}
