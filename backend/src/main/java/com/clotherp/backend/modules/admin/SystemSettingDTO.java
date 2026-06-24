package com.clotherp.backend.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDTO {
    private UUID id;
    private String key;
    private String value;
    private String description;
    private boolean publicAccess;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}