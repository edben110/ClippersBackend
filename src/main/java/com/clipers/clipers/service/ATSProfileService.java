package com.clipers.clipers.service;

import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.ATSProfileRepository;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestión de perfiles ATS
 */
@Service
@Transactional
public class ATSProfileService {

    private final ATSProfileRepository atsProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public ATSProfileService(ATSProfileRepository atsProfileRepository, UserRepository userRepository) {
        this.atsProfileRepository = atsProfileRepository;
        this.userRepository = userRepository;
    }

    public Optional<ATSProfile> findByUserId(String userId) {
        return atsProfileRepository.findByUserId(userId);
    }

    public ATSProfile createProfile(String userId, String summary, String cliperId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != User.Role.CANDIDATE) {
            throw new IllegalArgumentException("Solo los candidatos pueden crear perfiles ATS");
        }

        // Verificar si ya existe un perfil
        Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(userId);
        if (existingProfile.isPresent()) {
            throw new IllegalStateException("El usuario ya tiene un perfil ATS");
        }

        ATSProfile profile = new ATSProfile(user)
                .withSummary(summary)
                .withCliper(cliperId);

        return atsProfileRepository.save(profile);
    }

    public ATSProfile updateProfile(String userId, String summary) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.withSummary(summary);
        return atsProfileRepository.save(profile);
    }

    public ATSProfile updateFullProfile(String userId, Map<String, Object> updates) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        // Update summary if provided
        if (updates.containsKey("summary")) {
            profile.setSummary((String) updates.get("summary"));
        }

        // Update education if provided
        if (updates.containsKey("education")) {
            // Clear existing education and add new ones
            profile.getEducation().clear();
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> educationList = (java.util.List<Map<String, Object>>) updates.get("education");
            if (educationList != null) {
                for (Map<String, Object> edu : educationList) {
                    String institution = (String) edu.get("institution");
                    String degree = (String) edu.get("degree");
                    String field = (String) edu.get("field");
                    profile.addEducation(institution, degree, field);
                }
            }
        }

        // Update experience if provided
        if (updates.containsKey("experience")) {
            // Clear existing experience and add new ones
            profile.getExperience().clear();
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> experienceList = (java.util.List<Map<String, Object>>) updates.get("experience");
            if (experienceList != null) {
                for (Map<String, Object> exp : experienceList) {
                    String company = (String) exp.get("company");
                    String position = (String) exp.get("position");
                    String description = (String) exp.get("description");
                    profile.addExperience(company, position, description);
                }
            }
        }

        // Update skills if provided
        if (updates.containsKey("skills")) {
            // Clear existing skills and add new ones
            profile.getSkills().clear();
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> skillsList = (java.util.List<Map<String, Object>>) updates.get("skills");
            if (skillsList != null) {
                for (Map<String, Object> skill : skillsList) {
                    String name = (String) skill.get("name");
                    String category = (String) skill.get("category");
                    String level = (String) skill.get("level");
                    com.clipers.clipers.entity.Skill.SkillLevel skillLevel = com.clipers.clipers.entity.Skill.SkillLevel.valueOf(level.toUpperCase());
                    com.clipers.clipers.entity.Skill.SkillCategory skillCategory = com.clipers.clipers.entity.Skill.SkillCategory.valueOf(category.toUpperCase());
                    profile.addSkill(name, skillLevel, skillCategory);
                }
            }
        }

        // Update languages if provided
        if (updates.containsKey("languages")) {
            // Clear existing languages and add new ones
            profile.getLanguages().clear();
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> languagesList = (java.util.List<Map<String, Object>>) updates.get("languages");
            if (languagesList != null) {
                for (Map<String, Object> lang : languagesList) {
                    String name = (String) lang.get("name");
                    String level = (String) lang.get("level");
                    com.clipers.clipers.entity.Language.LanguageLevel languageLevel = com.clipers.clipers.entity.Language.LanguageLevel.valueOf(level.toUpperCase());
                    profile.addLanguage(name, languageLevel);
                }
            }
        }

        return atsProfileRepository.save(profile);
    }

    public void deleteProfile(String userId) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        atsProfileRepository.delete(profile);
    }

    // Education management methods
    public com.clipers.clipers.entity.Education addEducation(String userId, String institution, String degree, String field) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.addEducation(institution, degree, field);
        atsProfileRepository.save(profile);
        // Return the last added education
        return profile.getEducation().get(profile.getEducation().size() - 1);
    }

    public com.clipers.clipers.entity.Education updateEducation(String userId, String educationId, String institution, String degree, String field) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        com.clipers.clipers.entity.Education education = profile.getEducation().stream()
                .filter(e -> e.getId().equals(educationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Educación no encontrada"));

        education.setInstitution(institution);
        education.setDegree(degree);
        education.setField(field);

        atsProfileRepository.save(profile);
        return education;
    }

    public void deleteEducation(String userId, String educationId) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.getEducation().removeIf(e -> e.getId().equals(educationId));
        atsProfileRepository.save(profile);
    }

    // Experience management methods
    public com.clipers.clipers.entity.Experience addExperience(String userId, String company, String position, String description) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.addExperience(company, position, description);
        atsProfileRepository.save(profile);
        // Return the last added experience
        return profile.getExperience().get(profile.getExperience().size() - 1);
    }

    public com.clipers.clipers.entity.Experience updateExperience(String userId, String experienceId, String company, String position, String description) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        com.clipers.clipers.entity.Experience experience = profile.getExperience().stream()
                .filter(e -> e.getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Experiencia no encontrada"));

        experience.setCompany(company);
        experience.setPosition(position);
        experience.setDescription(description);

        atsProfileRepository.save(profile);
        return experience;
    }

    public void deleteExperience(String userId, String experienceId) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.getExperience().removeIf(e -> e.getId().equals(experienceId));
        atsProfileRepository.save(profile);
    }

    // Skills management methods
    public com.clipers.clipers.entity.Skill addSkill(String userId, String name, com.clipers.clipers.entity.Skill.SkillLevel level, com.clipers.clipers.entity.Skill.SkillCategory category) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.addSkill(name, level, category);
        atsProfileRepository.save(profile);
        // Return the last added skill
        return profile.getSkills().get(profile.getSkills().size() - 1);
    }

    public com.clipers.clipers.entity.Skill updateSkill(String userId, String skillId, String name, com.clipers.clipers.entity.Skill.SkillLevel level, com.clipers.clipers.entity.Skill.SkillCategory category) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        com.clipers.clipers.entity.Skill skill = profile.getSkills().stream()
                .filter(s -> s.getId().equals(skillId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Habilidad no encontrada"));

        skill.setName(name);
        skill.setLevel(level);
        skill.setCategory(category);

        atsProfileRepository.save(profile);
        return skill;
    }

    public void deleteSkill(String userId, String skillId) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        profile.getSkills().removeIf(s -> s.getId().equals(skillId));
        atsProfileRepository.save(profile);
    }
}
