package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    
    List<Job> findByCompanyId(String companyId);
    
    Page<Job> findByCompanyIdOrderByCreatedAtDesc(String companyId, Pageable pageable);
    
    List<Job> findByIsActiveTrue();
    
    Page<Job> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Job> findByType(Job.JobType type);
    
    List<Job> findByLocation(String location);
    
    @Query("{ 'isActive': true, $or: [ " +
           "{ 'title': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<Job> searchActiveJobs(String query, Pageable pageable);
    
    @Query("{ 'isActive': true, 'skills': { $regex: ?0, $options: 'i' } }")
    List<Job> findActiveJobsBySkill(String skill);
    
    @Query("{ 'isActive': true, 'type': ?0, 'location': { $regex: ?1, $options: 'i' }, 'salaryMin': { $gte: ?2 }, 'salaryMax': { $lte: ?3 } }")
    Page<Job> findJobsWithFilters(Job.JobType type, String location, Integer minSalary, Integer maxSalary, Pageable pageable);
    
    @Query(value = "{ 'isActive': true, 'location': { $ne: null } }", fields = "{ 'location': 1 }")
    List<Job> findAllActiveJobLocations();
}
