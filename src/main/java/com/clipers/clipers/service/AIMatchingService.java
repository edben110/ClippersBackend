package com.clipers.clipers.service;

import com.clipers.clipers.dto.matching.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for AI-powered candidate-job matching
 * Communicates with microSelectIA Python service
 */
@Service
public class AIMatchingService {

    private static final Logger logger = LoggerFactory.getLogger(AIMatchingService.class);

    private final RestTemplate restTemplate;

    @Value("${ai.matching.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${ai.matching.service.enabled:true}")
    private boolean aiServiceEnabled;

    public AIMatchingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Match a single candidate against a job
     */
    public SingleMatchResponseDTO matchSingleCandidate(CandidateDTO candidate, JobDTO job) {
        if (!aiServiceEnabled) {
            logger.warn("AI Matching service is disabled");
            return createFallbackSingleMatch(candidate, job);
        }

        try {
            String url = aiServiceUrl + "/api/match/single";
            
            BatchMatchRequestDTO request = new BatchMatchRequestDTO();
            request.setCandidates(List.of(candidate));
            request.setJob(job);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<BatchMatchRequestDTO> entity = new HttpEntity<>(request, headers);

            logger.info("Calling AI service for single match: candidate={}, job={}", 
                       candidate.getId(), job.getId());

            ResponseEntity<SingleMatchResponseDTO> response = restTemplate.postForEntity(
                url, entity, SingleMatchResponseDTO.class
            );

            logger.info("AI match completed successfully: score={}", 
                       response.getBody().getCompatibilityScore());

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error calling AI matching service: {}", e.getMessage(), e);
            return createFallbackSingleMatch(candidate, job);
        }
    }

    /**
     * Match multiple candidates against a single job
     * Returns candidates ranked by compatibility
     */
    public BatchMatchResponseDTO matchBatchCandidates(List<CandidateDTO> candidates, JobDTO job) {
        if (!aiServiceEnabled) {
            logger.warn("AI Matching service is disabled");
            return createFallbackBatchMatch(candidates, job);
        }

        try {
            String url = aiServiceUrl + "/api/match/batch";

            BatchMatchRequestDTO request = new BatchMatchRequestDTO();
            request.setCandidates(candidates);
            request.setJob(job);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<BatchMatchRequestDTO> entity = new HttpEntity<>(request, headers);

            logger.info("Calling AI service for batch match: {} candidates, job={}", 
                       candidates.size(), job.getId());

            ResponseEntity<BatchMatchResponseDTO> response = restTemplate.postForEntity(
                url, entity, BatchMatchResponseDTO.class
            );

            logger.info("Batch match completed: total={}, avg_score={}", 
                       response.getBody().getTotalCandidates(),
                       response.getBody().getAverageScore());

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error calling AI batch matching service: {}", e.getMessage(), e);
            return createFallbackBatchMatch(candidates, job);
        }
    }

    /**
     * Get detailed explanation of a candidate-job match
     */
    public ExplainMatchResponseDTO explainMatch(CandidateDTO candidate, JobDTO job, boolean includeSuggestions) {
        if (!aiServiceEnabled) {
            logger.warn("AI Matching service is disabled");
            return createFallbackExplanation(candidate, job);
        }

        try {
            String url = aiServiceUrl + "/api/match/explain";

            ExplainMatchRequestDTO request = new ExplainMatchRequestDTO();
            request.setCandidate(candidate);
            request.setJob(job);
            request.setIncludeSuggestions(includeSuggestions);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ExplainMatchRequestDTO> entity = new HttpEntity<>(request, headers);

            logger.info("Calling AI service for match explanation: candidate={}, job={}", 
                       candidate.getId(), job.getId());

            ResponseEntity<ExplainMatchResponseDTO> response = restTemplate.postForEntity(
                url, entity, ExplainMatchResponseDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error calling AI explain service: {}", e.getMessage(), e);
            return createFallbackExplanation(candidate, job);
        }
    }

    /**
     * Check if AI service is available
     */
    public HealthResponseDTO checkHealth() {
        try {
            String url = aiServiceUrl + "/health";
            ResponseEntity<HealthResponseDTO> response = restTemplate.getForEntity(
                url, HealthResponseDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("AI service health check failed: {}", e.getMessage());
            HealthResponseDTO health = new HealthResponseDTO();
            health.setStatus("unhealthy");
            health.setMessage("AI service unavailable: " + e.getMessage());
            return health;
        }
    }

    // Fallback methods for when AI service is unavailable

    private SingleMatchResponseDTO createFallbackSingleMatch(CandidateDTO candidate, JobDTO job) {
        SingleMatchResponseDTO response = new SingleMatchResponseDTO();
        response.setCandidateId(candidate.getId());
        response.setCandidateName(candidate.getName());
        response.setJobId(job.getId());
        response.setCompatibilityScore(0.5);
        response.setMatchPercentage(50);
        response.setExplanation("AI service unavailable - using fallback matching");
        response.setMatchQuality("medium");
        
        MatchBreakdownDTO breakdown = new MatchBreakdownDTO();
        breakdown.setSkillsMatch(0.5);
        breakdown.setExperienceMatch(0.5);
        breakdown.setEducationMatch(0.5);
        breakdown.setSemanticMatch(0.5);
        response.setBreakdown(breakdown);
        
        return response;
    }

    private BatchMatchResponseDTO createFallbackBatchMatch(List<CandidateDTO> candidates, JobDTO job) {
        BatchMatchResponseDTO response = new BatchMatchResponseDTO();
        response.setJobId(job.getId());
        response.setJobTitle(job.getTitle());
        response.setTotalCandidates(candidates.size());
        response.setAverageScore(0.5);
        return response;
    }

    private ExplainMatchResponseDTO createFallbackExplanation(CandidateDTO candidate, JobDTO job) {
        ExplainMatchResponseDTO response = new ExplainMatchResponseDTO();
        response.setCandidateId(candidate.getId());
        response.setJobId(job.getId());
        response.setCompatibilityScore(0.5);
        response.setMatchPercentage(50);
        response.setDecisionRecommendation("AI service unavailable - manual review recommended");
        
        MatchBreakdownDTO breakdown = new MatchBreakdownDTO();
        breakdown.setSkillsMatch(0.5);
        breakdown.setExperienceMatch(0.5);
        breakdown.setEducationMatch(0.5);
        breakdown.setSemanticMatch(0.5);
        response.setBreakdown(breakdown);
        
        return response;
    }
}
