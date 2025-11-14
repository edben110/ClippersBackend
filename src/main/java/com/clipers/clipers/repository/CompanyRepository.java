package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {
    
    Optional<Company> findByUserId(String userId);
    
    List<Company> findByIndustry(String industry);
    
    List<Company> findByLocation(String location);
    
    @Query("{ $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } }, " +
           "{ 'industry': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<Company> searchCompanies(String query);
    
    @Query(value = "{ 'industry': { $ne: null } }", fields = "{ 'industry': 1 }")
    List<Company> findAllIndustries();
    
    @Query(value = "{ 'location': { $ne: null } }", fields = "{ 'location': 1 }")
    List<Company> findAllLocations();
}
