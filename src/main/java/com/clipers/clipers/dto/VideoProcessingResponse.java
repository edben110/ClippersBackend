package com.clipers.clipers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoProcessingResponse {
    @JsonProperty("transcripcion")
    private String transcription;

    @JsonProperty("perfil")
    private Profile profile;

    public VideoProcessingResponse() {}

    public VideoProcessingResponse(String transcription, Profile profile) {
        this.transcription = transcription;
        this.profile = profile;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static class Profile {
        @JsonProperty("nombre")
        private Object name;

        @JsonProperty("profesion")
        private Object profession;

        @JsonProperty("experiencia")
        private Object experience;

        @JsonProperty("educacion")
        private Object education;

        @JsonProperty("tecnologias")
        private Object technologies;

        @JsonProperty("idiomas")
        private Object languages;

        @JsonProperty("logros")
        private Object achievements;

        @JsonProperty("habilidades_blandas")
        private Object softSkills;

        public Profile() {}

        public Profile(String name, String profession, String experience, String education,
                      String technologies, String languages, String achievements, String softSkills) {
            this.name = name;
            this.profession = profession;
            this.experience = experience;
            this.education = education;
            this.technologies = technologies;
            this.languages = languages;
            this.achievements = achievements;
            this.softSkills = softSkills;
        }

        // Getters and setters
        public String getName() { return convertToString(name); }
        public void setName(Object name) { this.name = name; }

        public String getProfession() { return convertToString(profession); }
        public void setProfession(Object profession) { this.profession = profession; }

        public String getExperience() { return convertToString(experience); }
        public void setExperience(Object experience) { this.experience = experience; }

        public String getEducation() { return convertToString(education); }
        public void setEducation(Object education) { this.education = education; }

        public String getTechnologies() { return convertToString(technologies); }
        public void setTechnologies(Object technologies) { this.technologies = technologies; }

        public String getLanguages() { return convertToString(languages); }
        public void setLanguages(Object languages) { this.languages = languages; }

        public String getAchievements() { return convertToString(achievements); }
        public void setAchievements(Object achievements) { this.achievements = achievements; }

        public String getSoftSkills() { return convertToString(softSkills); }
        public void setSoftSkills(Object softSkills) { this.softSkills = softSkills; }

        private String convertToString(Object obj) {
            if (obj == null) {
                return "No especificado";
            }
            if (obj instanceof String) {
                return (String) obj;
            }
            if (obj instanceof java.util.List) {
                return String.join(", ", ((java.util.List<?>) obj).stream()
                    .map(Object::toString)
                    .toArray(String[]::new));
            }
            return obj.toString();
        }
    }
}