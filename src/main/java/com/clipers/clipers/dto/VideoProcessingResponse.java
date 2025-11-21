package com.clipers.clipers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoProcessingResponse {
    @JsonProperty("cv_profile")
    private String cvProfile;

    @JsonProperty("profile_data")
    private Profile profileData;

    public VideoProcessingResponse() {}

    public VideoProcessingResponse(String cvProfile, Profile profileData) {
        this.cvProfile = cvProfile;
        this.profileData = profileData;
    }

    public String getCvProfile() {
        return cvProfile;
    }

    public void setCvProfile(String cvProfile) {
        this.cvProfile = cvProfile;
    }

    public Profile getProfileData() {
        return profileData;
    }

    public void setProfileData(Profile profileData) {
        this.profileData = profileData;
    }

    // Mantener compatibilidad con código existente
    public String getTranscription() {
        return cvProfile;
    }

    public void setTranscription(String transcription) {
        this.cvProfile = transcription;
    }

    public Profile getProfile() {
        return profileData;
    }

    public void setProfile(Profile profile) {
        this.profileData = profile;
    }

    public static class Profile {
        @JsonProperty("name")
        private Object name;

        @JsonProperty("profession")
        private Object profession;

        @JsonProperty("experience")
        private Object experience;

        @JsonProperty("education")
        private Object education;

        @JsonProperty("technologies")
        private Object technologies;

        @JsonProperty("languages")
        private Object languages;

        @JsonProperty("achievements")
        private Object achievements;

        @JsonProperty("soft_skills")
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