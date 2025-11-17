package com.clipers.clipers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TechnicalTestResponse {
    
    @JsonProperty("technical_test_markdown")
    private String technicalTestMarkdown;
    
    @JsonProperty("profile_summary")
    private ProfileSummary profileSummary;
    
    public TechnicalTestResponse() {}
    
    public String getTechnicalTestMarkdown() {
        return technicalTestMarkdown;
    }
    
    public void setTechnicalTestMarkdown(String technicalTestMarkdown) {
        this.technicalTestMarkdown = technicalTestMarkdown;
    }
    
    public ProfileSummary getProfileSummary() {
        return profileSummary;
    }
    
    public void setProfileSummary(ProfileSummary profileSummary) {
        this.profileSummary = profileSummary;
    }
    
    public static class ProfileSummary {
        private String profession;
        private String technologies;
        private String experience;
        
        public ProfileSummary() {}
        
        public String getProfession() {
            return profession;
        }
        
        public void setProfession(String profession) {
            this.profession = profession;
        }
        
        public String getTechnologies() {
            return technologies;
        }
        
        public void setTechnologies(String technologies) {
            this.technologies = technologies;
        }
        
        public String getExperience() {
            return experience;
        }
        
        public void setExperience(String experience) {
            this.experience = experience;
        }
    }
}
