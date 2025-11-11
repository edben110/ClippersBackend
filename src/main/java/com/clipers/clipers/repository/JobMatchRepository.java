package com.clipers.clipers.repository;

import com.clipers.clipers.entity.JobMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, String> {
    
    List<JobMatch> findByUserId(String userId);
    
    List<JobMatch> findByJobId(String jobId);
    
    Optional<JobMatch> findByUserIdAndJobId(String userId, String jobId);
    
    Page<JobMatch> findByUserIdOrderByScoreDesc(String userId, Pageable pageable);
    
    Page<JobMatch> findByJobIdOrderByScoreDesc(String jobId, Pageable pageable);
    
    @Query("SELECT jm FROM JobMatch jm WHERE jm.user.id = :userId AND jm.score >= :minScore ORDER BY jm.score DESC")
    List<JobMatch> findHighScoringMatchesForUser(@Param("userId") String userId, @Param("minScore") Double minScore);
    
    @Query("SELECT jm FROM JobMatch jm WHERE jm.job.id = :jobId AND jm.score >= :minScore ORDER BY jm.score DESC")
    List<JobMatch> findHighScoringMatchesForJob(@Param("jobId") String jobId, @Param("minScore") Double minScore);
    
    @Query("SELECT AVG(jm.score) FROM JobMatch jm WHERE jm.user.id = :userId")
    Double getAverageScoreForUser(@Param("userId") String userId);
}
