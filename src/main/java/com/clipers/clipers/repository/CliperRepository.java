package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Cliper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CliperRepository extends MongoRepository<Cliper, String> {
    
    List<Cliper> findByUserId(String userId);
    
    Page<Cliper> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Cliper> findByStatus(Cliper.Status status);
    
    Page<Cliper> findByStatusOrderByCreatedAtDesc(Cliper.Status status, Pageable pageable);
    
    @Query("{ $or: [ " +
           "{ 'title': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<Cliper> searchClipers(String query, Pageable pageable);
    
    @Query("{ 'skills': { $regex: ?0, $options: 'i' } }")
    List<Cliper> findBySkillsContaining(String skill);
}
