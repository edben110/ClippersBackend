package com.clipers.clipers.repository;

import com.clipers.clipers.entity.ATSProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ATSProfileRepository extends MongoRepository<ATSProfile, String> {

    Optional<ATSProfile> findByUserId(String userId);
    
    Optional<ATSProfile> findByCliperId(String cliperId);
    
    @Query("{ 'summary': { $regex: ?0, $options: 'i' } }")
    List<ATSProfile> searchBySummary(String query);
    
    @Query("{ 'skills.name': { $regex: ?0, $options: 'i' } }")
    List<ATSProfile> findBySkillsContaining(String skill);
    
    Long countByUserId(String userId);
}
