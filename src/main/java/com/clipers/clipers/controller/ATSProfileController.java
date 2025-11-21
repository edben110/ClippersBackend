package com.clipers.clipers.controller;

import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.repository.UserRepository;
import com.clipers.clipers.service.ATSProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ats-profiles")
@CrossOrigin(origins = "*")
public class ATSProfileController {

    private final ATSProfileService atsProfileService;
    private final UserRepository userRepository;

    @Autowired
    public ATSProfileController(ATSProfileService atsProfileService, UserRepository userRepository) {
        this.atsProfileService = atsProfileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ATSProfile> getMyProfile() {
        try {
            String userId = getCurrentUserId();
            return atsProfileService.findByUserId(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error getting ATS profile: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ATSProfile> getProfile(@PathVariable String userId) {
        return atsProfileService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ATSProfile> updateMyProfile(@RequestBody Map<String, Object> updates) {
        try {
            String userId = getCurrentUserId();
            ATSProfile profile = atsProfileService.updateFullProfile(userId, updates);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar perfil ATS: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteMyProfile() {
        try {
            String userId = getCurrentUserId();
            atsProfileService.deleteProfile(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar perfil ATS: " + e.getMessage(), e);
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}
