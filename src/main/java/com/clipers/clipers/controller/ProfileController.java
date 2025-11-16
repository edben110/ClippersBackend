package com.clipers.clipers.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.repository.UserRepository;
import com.clipers.clipers.service.ATSProfileService;

/**
 * Controlador para gestión de perfiles ATS
 */
@RestController
@RequestMapping("/api/ats-profiles")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ATSProfileService atsProfileService;
    private final UserRepository userRepository;

    @Autowired
    public ProfileController(ATSProfileService atsProfileService, UserRepository userRepository) {
        this.atsProfileService = atsProfileService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE') and !hasRole('COMPANY') and !hasRole('ADMIN')")
    public ResponseEntity<ATSProfile> getATSProfile() {
        try {
            String userId = getCurrentUserId();
            return atsProfileService.findByUserId(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener perfil ATS: " + e.getMessage(), e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ATSProfile> getATSProfileByUserId(@PathVariable String userId) {
        return atsProfileService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ats")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ATSProfile> createATSProfile(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            String summary = (String) request.get("summary");
            String cliperId = (String) request.get("cliperId");
            
            ATSProfile profile = atsProfileService.createProfile(userId, summary, cliperId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear perfil ATS: " + e.getMessage(), e);
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ATSProfile> updateATSProfile(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();

            ATSProfile profile = atsProfileService.updateFullProfile(userId, request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar perfil ATS: " + e.getMessage(), e);
        }
    }

    // Education endpoints
    @PostMapping("/education")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Education> addEducation(@RequestBody com.clipers.clipers.dto.EducationRequest educationData) {
        try {
            String userId = getCurrentUserId();
            
            com.clipers.clipers.entity.Education education = atsProfileService.addEducation(
                userId, 
                educationData.getInstitution(), 
                educationData.getDegree(), 
                educationData.getField(),
                educationData.getStartDate(),
                educationData.getEndDate(),
                educationData.getDescription()
            );
            return ResponseEntity.ok(education);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar educación: " + e.getMessage(), e);
        }
    }

    @PutMapping("/education/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Education> updateEducation(@PathVariable String id, @RequestBody com.clipers.clipers.dto.EducationRequest educationData) {
        try {
            String userId = getCurrentUserId();
            com.clipers.clipers.entity.Education education = atsProfileService.updateEducation(
                userId, 
                Integer.parseInt(id), 
                educationData.getInstitution(), 
                educationData.getDegree(), 
                educationData.getField(),
                educationData.getStartDate(),
                educationData.getEndDate(),
                educationData.getDescription()
            );
            
            return ResponseEntity.ok(education);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar educación: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/education/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteEducation(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            atsProfileService.deleteEducation(userId, Integer.parseInt(id));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar educación: " + e.getMessage(), e);
        }
    }

    // Experience endpoints
    @PostMapping("/experience")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Experience> addExperience(@RequestBody com.clipers.clipers.dto.ExperienceRequest experienceData) {
        try {
            String userId = getCurrentUserId();
            
            com.clipers.clipers.entity.Experience experience = atsProfileService.addExperience(
                userId, 
                experienceData.getCompany(), 
                experienceData.getPosition(), 
                experienceData.getDescription(),
                experienceData.getStartDate(),
                experienceData.getEndDate(),
                experienceData.getSkills()
            );
            return ResponseEntity.ok(experience);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar experiencia: " + e.getMessage(), e);
        }
    }

    @PutMapping("/experience/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Experience> updateExperience(@PathVariable String id, @RequestBody com.clipers.clipers.dto.ExperienceRequest experienceData) {
        try {
            String userId = getCurrentUserId();
            com.clipers.clipers.entity.Experience experience = atsProfileService.updateExperience(
                userId, 
                Integer.parseInt(id), 
                experienceData.getCompany(), 
                experienceData.getPosition(), 
                experienceData.getDescription(),
                experienceData.getStartDate(),
                experienceData.getEndDate(),
                experienceData.getSkills()
            );
            
            return ResponseEntity.ok(experience);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar experiencia: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/experience/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteExperience(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            atsProfileService.deleteExperience(userId, Integer.parseInt(id));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar experiencia: " + e.getMessage(), e);
        }
    }

    // Skills endpoints
    @PostMapping("/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Skill> addSkill(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String name = request.get("name");
            String level = request.get("level");
            String category = request.get("category");

            // Validar que los campos no estén vacíos
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de la habilidad es requerido");
            }
            if (level == null || level.trim().isEmpty()) {
                throw new IllegalArgumentException("El nivel de la habilidad es requerido");
            }
            if (category == null || category.trim().isEmpty()) {
                throw new IllegalArgumentException("La categoría de la habilidad es requerida");
            }

            com.clipers.clipers.entity.Skill.SkillLevel skillLevel = com.clipers.clipers.entity.Skill.SkillLevel.valueOf(level.toUpperCase());
            com.clipers.clipers.entity.Skill.SkillCategory skillCategory = com.clipers.clipers.entity.Skill.SkillCategory.valueOf(category.toUpperCase());

            com.clipers.clipers.entity.Skill skill = atsProfileService.addSkill(userId, name, skillLevel, skillCategory);
            return ResponseEntity.ok(skill);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error al agregar habilidad: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar habilidad: " + e.getMessage(), e);
        }
    }

    @PutMapping("/skills/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.Skill> updateSkill(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String name = request.get("name");
            String level = request.get("level");
            String category = request.get("category");

            com.clipers.clipers.entity.Skill.SkillLevel skillLevel = com.clipers.clipers.entity.Skill.SkillLevel.valueOf(level.toUpperCase());
            com.clipers.clipers.entity.Skill.SkillCategory skillCategory = com.clipers.clipers.entity.Skill.SkillCategory.valueOf(category.toUpperCase());

            com.clipers.clipers.entity.Skill skill = atsProfileService.updateSkill(userId, Integer.parseInt(id), name, skillLevel, skillCategory);
            return ResponseEntity.ok(skill);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar habilidad: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/skills/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteSkill(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            atsProfileService.deleteSkill(userId, Integer.parseInt(id));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar habilidad: " + e.getMessage(), e);
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Obtener el email del JWT token y buscar el usuario real en la base de datos
        String email = auth.getName();
        // Buscar el usuario por email y devolver su ID real
        return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}
