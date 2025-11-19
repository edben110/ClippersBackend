package com.clipers.clipers.repository;

import com.clipers.clipers.entity.AIMatchResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIMatchResultRepository extends MongoRepository<AIMatchResult, String> {
    
    /**
     * Find all match results for a specific job, ordered by rank
     */
    List<AIMatchResult> findByJobIdOrderByRankAsc(String jobId);
    
    /**
     * Find all match results for a specific job, ordered by compatibility score
     */
    List<AIMatchResult> findByJobIdOrderByCompatibilityScoreDesc(String jobId);
    
    /**
     * Find match result for a specific candidate and job
     */
    Optional<AIMatchResult> findByJobIdAndCandidateId(String jobId, String candidateId);
    
    /**
     * Find all match results from a specific batch
     */
    List<AIMatchResult> findByBatchIdOrderByRankAsc(String batchId);
    
    /**
     * Find the latest batch results for a job
     */
    @Query(value = "{ 'jobId': ?0 }", sort = "{ 'createdAt': -1 }")
    List<AIMatchResult> findLatestByJobId(String jobId);
    
    /**
     * Delete old match results for a job (cleanup)
     */
    void deleteByJobIdAndCreatedAtBefore(String jobId, LocalDateTime date);
    
    /**
     * Count match results for a job
     */
    long countByJobId(String jobId);
    
    /**
     * Find top N candidates for a job
     */
    @Query(value = "{ 'jobId': ?0 }", sort = "{ 'compatibilityScore': -1 }")
    List<AIMatchResult> findTopCandidatesForJob(String jobId);
}
