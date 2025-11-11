package com.clipers.clipers.repository;

import com.clipers.clipers.entity.ATSProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ATSProfileRepository extends JpaRepository<ATSProfile, String> {

    Optional<ATSProfile> findByUserId(String userId);
    
    Optional<ATSProfile> findByCliperId(String cliperId);
    
    @Query("SELECT a FROM ATSProfile a WHERE " +
           "LOWER(a.summary) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ATSProfile> searchBySummary(@Param("query") String query);
    
    @Query("SELECT a FROM ATSProfile a JOIN a.skills s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<ATSProfile> findBySkillsContaining(@Param("skill") String skill);
    
    @Query("SELECT COUNT(a) FROM ATSProfile a WHERE a.user.role = 'CANDIDATE'")
    Long countCandidateProfiles();
}
