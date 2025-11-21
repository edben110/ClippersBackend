package com.clipers.clipers.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "technical_tests")
public class TechnicalTest {
    
    @Id
    private String id;
    
    private String jobId;
    private String candidateId;
    private String companyId;
    private String companyName; // Company name
    private String jobTitle; // Job title
    private String testMarkdown; // Old format (maintain compatibility)
    private String testJson; // New structured JSON format
    private TestStatus status;
    private String candidateResponse; // Respuesta del candidato (formato antiguo)
    private String candidateAnswersJson; // Structured JSON answers
    private Integer score; // Test score
    private String feedback; // Company feedback
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime submittedAt; // When candidate submitted response
    private LocalDateTime reviewedAt; // When company reviewed
    
    public enum TestStatus {
        SENT,       // Sent to candidate
        IN_PROGRESS, // Candidate is working on it
        SUBMITTED,  // Candidate submitted it
        REVIEWED    // Company reviewed it
    }
    
    public TechnicalTest() {}
    
    public TechnicalTest(String jobId, String candidateId, String companyId, String testMarkdown) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.companyId = companyId;
        this.testMarkdown = testMarkdown;
        this.status = TestStatus.SENT;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getCandidateId() {
        return candidateId;
    }
    
    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    
    public String getTestMarkdown() {
        return testMarkdown;
    }
    
    public void setTestMarkdown(String testMarkdown) {
        this.testMarkdown = testMarkdown;
    }
    
    public TestStatus getStatus() {
        return status;
    }
    
    public void setStatus(TestStatus status) {
        this.status = status;
    }
    
    public String getCandidateResponse() {
        return candidateResponse;
    }
    
    public void setCandidateResponse(String candidateResponse) {
        this.candidateResponse = candidateResponse;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getTestJson() {
        return testJson;
    }
    
    public void setTestJson(String testJson) {
        this.testJson = testJson;
    }
    
    public String getCandidateAnswersJson() {
        return candidateAnswersJson;
    }
    
    public void setCandidateAnswersJson(String candidateAnswersJson) {
        this.candidateAnswersJson = candidateAnswersJson;
    }
}
