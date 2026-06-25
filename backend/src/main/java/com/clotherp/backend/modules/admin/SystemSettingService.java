package com.clotherp.backend.modules.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository repository;

    @Transactional(readOnly = true)
    public List<SystemSettingDTO> getAllSettings() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SystemSettingDTO getSettingByKey(String key) {
        return repository.findByKey(key)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
    }

    @Transactional
    public SystemSettingDTO createOrUpdateSetting(String key, String value, String description, boolean publicAccess) {
        SystemSetting setting = repository.findByKey(key)
                .orElse(SystemSetting.builder().key(key).build());
        setting.setValue(value);
        setting.setDescription(description);
        setting.setPublicAccess(publicAccess);
        return toDTO(repository.save(setting));
    }

    @Transactional
    public void deleteSetting(UUID id) {
        repository.deleteById(id);
    }

    private SystemSettingDTO toDTO(SystemSetting entity) {
        return SystemSettingDTO.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .value(entity.getValue())
                .description(entity.getDescription())
                .publicAccess(entity.isPublicAccess())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}