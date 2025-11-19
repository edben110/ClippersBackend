package com.clipers.clipers.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stores AI matching results for persistence
 * Allows companies to see matching results even after page refresh
 */
@Document(collection = "ai_match_results")
public class AIMatchResult {

    @Id
    private String id;

    @Indexed
    private String jobId;

    private String candidateId;

    private String candidateName;

    private String candidateEmail;

    private Double compatibilityScore; // 0.0 to 1.0

    private Integer matchPercentage; // 0 to 100

    private Integer rank; // Position in ranking

    private String matchQuality; // excellent, good, medium, poor

    private String explanation;

    // Breakdown scores
    private Double skillsMatch;
    private Double experienceMatch;
    private Double educationMatch;
    private Double semanticMatch;

    // Matched and missing skills
    private List<String> matchedSkills;
    private List<String> missingSkills;

    // AI recommendations
    private List<String> recommendations;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Metadata
    private String batchId; // To group results from same matching session
    private Integer totalCandidatesInBatch;
    private Double averageScoreInBatch;

    // Constructors
    public AIMatchResult() {}

    public AIMatchResult(String jobId, String candidateId, String candidateName, 
                        Double compatibilityScore, Integer matchPercentage) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.compatibilityScore = compatibilityScore;
        this.matchPercentage = matchPercentage;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public Double getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public Integer getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Integer matchPercentage) { this.matchPercentage = matchPercentage; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getMatchQuality() { return matchQuality; }
    public void setMatchQuality(String matchQuality) { this.matchQuality = matchQuality; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Double getSkillsMatch() { return skillsMatch; }
    public void setSkillsMatch(Double skillsMatch) { this.skillsMatch = skillsMatch; }

    public Double getExperienceMatch() { return experienceMatch; }
    public void setExperienceMatch(Double experienceMatch) { this.experienceMatch = experienceMatch; }

    public Double getEducationMatch() { return educationMatch; }
    public void setEducationMatch(Double educationMatch) { this.educationMatch = educationMatch; }

    public Double getSemanticMatch() { return semanticMatch; }
    public void setSemanticMatch(Double semanticMatch) { this.semanticMatch = semanticMatch; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public Integer getTotalCandidatesInBatch() { return totalCandidatesInBatch; }
    public void setTotalCandidatesInBatch(Integer totalCandidatesInBatch) { 
        this.totalCandidatesInBatch = totalCandidatesInBatch; 
    }

    public Double getAverageScoreInBatch() { return averageScoreInBatch; }
    public void setAverageScoreInBatch(Double averageScoreInBatch) { 
        this.averageScoreInBatch = averageScoreInBatch; 
    }
}
