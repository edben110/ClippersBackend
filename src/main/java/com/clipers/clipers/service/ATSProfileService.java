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

        ATSProfile profile = new ATSProfile(user.getId())
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
    public com.clipers.clipers.entity.Education addEducation(String userId, String institution, String degree, String field, String startDate, String endDate, String description) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        com.clipers.clipers.entity.Education education = new com.clipers.clipers.entity.Education();
        education.setInstitution(institution);
        education.setDegree(degree);
        education.setField(field);
        education.setStartDate(startDate);
        education.setEndDate(endDate);
        education.setDescription(description);
        
        profile.getEducation().add(education);
        atsProfileRepository.save(profile);
        
        return education;
    }

    public com.clipers.clipers.entity.Education updateEducation(String userId, int educationIndex, String institution, String degree, String field, String startDate, String endDate, String description) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (educationIndex < 0 || educationIndex >= profile.getEducation().size()) {
            throw new RuntimeException("Índice de educación inválido");
        }

        com.clipers.clipers.entity.Education education = profile.getEducation().get(educationIndex);
        if (institution != null) education.setInstitution(institution);
        if (degree != null) education.setDegree(degree);
        if (field != null) education.setField(field);
        if (startDate != null) education.setStartDate(startDate);
        if (endDate != null) education.setEndDate(endDate);
        if (description != null) education.setDescription(description);

        atsProfileRepository.save(profile);
        return education;
    }

    public void deleteEducation(String userId, int educationIndex) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (educationIndex < 0 || educationIndex >= profile.getEducation().size()) {
            throw new RuntimeException("Índice de educación inválido");
        }

        profile.getEducation().remove(educationIndex);
        atsProfileRepository.save(profile);
    }

    // Experience management methods
    public com.clipers.clipers.entity.Experience addExperience(String userId, String company, String position, String description, String startDate, String endDate, java.util.List<String> skills) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        com.clipers.clipers.entity.Experience experience = new com.clipers.clipers.entity.Experience();
        experience.setCompany(company);
        experience.setPosition(position);
        experience.setDescription(description);
        experience.setStartDate(startDate);
        experience.setEndDate(endDate);
        experience.setSkills(skills);
        
        profile.getExperience().add(experience);
        atsProfileRepository.save(profile);
        
        return experience;
    }

    public com.clipers.clipers.entity.Experience updateExperience(String userId, int experienceIndex, String company, String position, String description, String startDate, String endDate, java.util.List<String> skills) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (experienceIndex < 0 || experienceIndex >= profile.getExperience().size()) {
            throw new RuntimeException("Índice de experiencia inválido");
        }

        com.clipers.clipers.entity.Experience experience = profile.getExperience().get(experienceIndex);
        if (company != null) experience.setCompany(company);
        if (position != null) experience.setPosition(position);
        if (description != null) experience.setDescription(description);
        if (startDate != null) experience.setStartDate(startDate);
        if (endDate != null) experience.setEndDate(endDate);
        if (skills != null) experience.setSkills(skills);

        atsProfileRepository.save(profile);
        return experience;
    }

    public void deleteExperience(String userId, int experienceIndex) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (experienceIndex < 0 || experienceIndex >= profile.getExperience().size()) {
            throw new RuntimeException("Índice de experiencia inválido");
        }

        profile.getExperience().remove(experienceIndex);
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

    public com.clipers.clipers.entity.Skill updateSkill(String userId, int skillIndex, String name, com.clipers.clipers.entity.Skill.SkillLevel level, com.clipers.clipers.entity.Skill.SkillCategory category) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (skillIndex < 0 || skillIndex >= profile.getSkills().size()) {
            throw new RuntimeException("Índice de habilidad inválido");
        }

        com.clipers.clipers.entity.Skill skill = profile.getSkills().get(skillIndex);
        skill.setName(name);
        skill.setLevel(level);
        skill.setCategory(category);

        atsProfileRepository.save(profile);
        return skill;
    }

    public void deleteSkill(String userId, int skillIndex) {
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil ATS no encontrado"));

        if (skillIndex < 0 || skillIndex >= profile.getSkills().size()) {
            throw new RuntimeException("Índice de habilidad inválido");
        }

        profile.getSkills().remove(skillIndex);
        atsProfileRepository.save(profile);
    }
}
