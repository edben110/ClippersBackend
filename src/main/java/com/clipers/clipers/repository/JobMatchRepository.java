package com.clipers.clipers.repository;

import com.clipers.clipers.entity.JobMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends MongoRepository<JobMatch, String> {
    
    List<JobMatch> findByUserId(String userId);
    
    List<JobMatch> findByJobId(String jobId);
    
    Optional<JobMatch> findByUserIdAndJobId(String userId, String jobId);
    
    Page<JobMatch> findByUserIdOrderByScoreDesc(String userId, Pageable pageable);
    
    Page<JobMatch> findByJobIdOrderByScoreDesc(String jobId, Pageable pageable);
    
    @Query(value = "{ 'userId': ?0, 'score': { $gte: ?1 } }", sort = "{ 'score': -1 }")
    List<JobMatch> findHighScoringMatchesForUser(String userId, Double minScore);
    
    @Query(value = "{ 'jobId': ?0, 'score': { $gte: ?1 } }", sort = "{ 'score': -1 }")
    List<JobMatch> findHighScoringMatchesForJob(String jobId, Double minScore);
    
    @Query(value = "{ 'userId': ?0 }", fields = "{ 'score': 1 }")
    List<JobMatch> findScoresByUserId(String userId);
}
