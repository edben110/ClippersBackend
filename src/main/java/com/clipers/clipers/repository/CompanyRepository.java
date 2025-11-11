package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    
    Optional<Company> findByUserId(String userId);
    
    List<Company> findByIndustry(String industry);
    
    List<Company> findByLocation(String location);
    
    @Query("SELECT c FROM Company c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Company> searchCompanies(@Param("query") String query);
    
    @Query("SELECT DISTINCT c.industry FROM Company c WHERE c.industry IS NOT NULL")
    List<String> findAllIndustries();
    
    @Query("SELECT DISTINCT c.location FROM Company c WHERE c.location IS NOT NULL")
    List<String> findAllLocations();
}
