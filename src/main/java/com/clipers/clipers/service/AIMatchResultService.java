package com.clipers.clipers.service;

import com.clipers.clipers.dto.matching.BatchMatchResponseDTO;
import com.clipers.clipers.dto.matching.RankedMatchResultDTO;
import com.clipers.clipers.entity.AIMatchResult;
import com.clipers.clipers.repository.AIMatchResultRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to persist and retrieve AI matching results
 * Allows results to survive page refreshes
 */
@Service
public class AIMatchResultService {

    private static final Logger logger = LoggerFactory.getLogger(AIMatchResultService.class);

    private final AIMatchResultRepository aiMatchResultRepository;

    public AIMatchResultService(AIMatchResultRepository aiMatchResultRepository) {
        this.aiMatchResultRepository = aiMatchResultRepository;
    }

    /**
     * Save batch matching results to database
     * Generates a unique batchId to group results from same matching session
     */
    public String saveBatchResults(BatchMatchResponseDTO batchResponse) {
        String batchId = UUID.randomUUID().toString();
        String jobId = batchResponse.getJobId();
        
        logger.info("Saving batch results: jobId={}, batchId={}, totalCandidates={}", 
                   jobId, batchId, batchResponse.getTotalCandidates());

        // Delete old results for this job (optional - keep only latest)
        deleteOldResults(jobId);

        // Convert and save each match result
        List<AIMatchResult> results = batchResponse.getMatches().stream()
            .map(match -> convertToEntity(match, jobId, batchId, batchResponse))
            .collect(Collectors.toList());

        aiMatchResultRepository.saveAll(results);
        
        logger.info("Saved {} match results for job {}", results.size(), jobId);
        
        return batchId;
    }

    /**
     * Get saved match results for a job
     * Returns the latest batch of results
     */
    public List<AIMatchResult> getMatchResultsForJob(String jobId) {
        logger.info("Retrieving match results for job {}", jobId);
        
        List<AIMatchResult> results = aiMatchResultRepository
            .findByJobIdOrderByRankAsc(jobId);
        
        logger.info("Found {} saved match results for job {}", results.size(), jobId);
        
        return results;
    }

    /**
     * Get match result for a specific candidate and job
     */
    public AIMatchResult getMatchResult(String jobId, String candidateId) {
        return aiMatchResultRepository
            .findByJobIdAndCandidateId(jobId, candidateId)
            .orElse(null);
    }

    /**
     * Check if match results exist for a job
     */
    public boolean hasMatchResults(String jobId) {
        return aiMatchResultRepository.countByJobId(jobId) > 0;
    }

    /**
     * Delete old match results for a job
     * Keeps only results from last 7 days
     */
    public void deleteOldResults(String jobId) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        aiMatchResultRepository.deleteByJobIdAndCreatedAtBefore(jobId, cutoffDate);
    }

    /**
     * Delete all match results for a job
     */
    public void deleteAllResultsForJob(String jobId) {
        List<AIMatchResult> results = aiMatchResultRepository.findByJobIdOrderByRankAsc(jobId);
        aiMatchResultRepository.deleteAll(results);
        logger.info("Deleted all match results for job {}", jobId);
    }

    /**
     * Convert DTO to entity
     */
    private AIMatchResult convertToEntity(RankedMatchResultDTO dto, String jobId, 
                                         String batchId, BatchMatchResponseDTO batchResponse) {
        AIMatchResult entity = new AIMatchResult();
        
        entity.setJobId(jobId);
        entity.setCandidateId(dto.getCandidateId());
        entity.setCandidateName(dto.getCandidateName());
        // candidateEmail is optional and not in DTO
        entity.setCompatibilityScore(dto.getCompatibilityScore());
        entity.setMatchPercentage(dto.getMatchPercentage());
        entity.setRank(dto.getRank());
        entity.setMatchQuality(dto.getMatchQuality());
        entity.setExplanation(dto.getExplanation());
        
        // Breakdown
        if (dto.getBreakdown() != null) {
            entity.setSkillsMatch(dto.getBreakdown().getSkillsMatch());
            entity.setExperienceMatch(dto.getBreakdown().getExperienceMatch());
            entity.setEducationMatch(dto.getBreakdown().getEducationMatch());
            entity.setSemanticMatch(dto.getBreakdown().getSemanticMatch());
        }
        
        // Skills
        entity.setMatchedSkills(dto.getMatchedSkills());
        entity.setMissingSkills(dto.getMissingSkills());
        entity.setRecommendations(dto.getRecommendations());
        
        // Batch metadata
        entity.setBatchId(batchId);
        entity.setTotalCandidatesInBatch(batchResponse.getTotalCandidates());
        entity.setAverageScoreInBatch(batchResponse.getAverageScore());
        
        return entity;
    }

    /**
     * Convert entity back to DTO for API responses
     */
    public RankedMatchResultDTO convertToDTO(AIMatchResult entity) {
        RankedMatchResultDTO dto = new RankedMatchResultDTO();
        
        dto.setCandidateId(entity.getCandidateId());
        dto.setCandidateName(entity.getCandidateName());
        // candidateEmail is not in DTO, skip it
        dto.setCompatibilityScore(entity.getCompatibilityScore());
        dto.setMatchPercentage(entity.getMatchPercentage());
        dto.setRank(entity.getRank());
        dto.setMatchQuality(entity.getMatchQuality());
        dto.setExplanation(entity.getExplanation());
        dto.setMatchedSkills(entity.getMatchedSkills());
        dto.setMissingSkills(entity.getMissingSkills());
        dto.setRecommendations(entity.getRecommendations());
        
        // Breakdown
        com.clipers.clipers.dto.matching.MatchBreakdownDTO breakdown = 
            new com.clipers.clipers.dto.matching.MatchBreakdownDTO();
        breakdown.setSkillsMatch(entity.getSkillsMatch());
        breakdown.setExperienceMatch(entity.getExperienceMatch());
        breakdown.setEducationMatch(entity.getEducationMatch());
        breakdown.setSemanticMatch(entity.getSemanticMatch());
        dto.setBreakdown(breakdown);
        
        return dto;
    }
}
