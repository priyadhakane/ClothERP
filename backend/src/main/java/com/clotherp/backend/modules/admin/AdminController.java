package com.clotherp.backend.modules.admin;

import com.clotherp.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')") // only super admin can access
public class AdminController {

    private final AdminService adminService;
    private final SystemSettingService systemSettingService;

    // ── Dashboard ─────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // ── System Settings ──────────────────────────────────────────────

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<SystemSettingDTO>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.ok(systemSettingService.getAllSettings()));
    }

    @GetMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<SystemSettingDTO>> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.ok(systemSettingService.getSettingByKey(key)));
    }

    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSettingDTO>> createOrUpdateSetting(
            @RequestBody @Valid SystemSettingDTO request) {
        SystemSettingDTO dto = systemSettingService.createOrUpdateSetting(
                request.getKey(),
                request.getValue(),
                request.getDescription(),
                request.isPublicAccess()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "Setting saved"));
    }

    @DeleteMapping("/settings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable UUID id) {
        systemSettingService.deleteSetting(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Setting deleted"));
    }
}