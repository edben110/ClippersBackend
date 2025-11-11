package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    
    List<Job> findByCompanyId(String companyId);
    
    Page<Job> findByCompanyIdOrderByCreatedAtDesc(String companyId, Pageable pageable);
    
    List<Job> findByIsActiveTrue();
    
    Page<Job> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<Job> findByType(Job.JobType type);
    
    List<Job> findByLocation(String location);
    
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Job> searchActiveJobs(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT j FROM Job j JOIN j.skills s WHERE " +
           "j.isActive = true AND LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Job> findActiveJobsBySkill(@Param("skill") String skill);
    
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "j.type = :type AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minSalary IS NULL OR j.salaryMin >= :minSalary) AND " +
           "(:maxSalary IS NULL OR j.salaryMax <= :maxSalary)")
    Page<Job> findJobsWithFilters(
        @Param("type") Job.JobType type,
        @Param("location") String location,
        @Param("minSalary") Integer minSalary,
        @Param("maxSalary") Integer maxSalary,
        Pageable pageable
    );
    
    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.isActive = true AND j.location IS NOT NULL")
    List<String> findAllActiveJobLocations();
}
