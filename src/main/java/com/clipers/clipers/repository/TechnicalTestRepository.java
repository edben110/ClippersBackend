package com.clipers.clipers.repository;

import com.clipers.clipers.entity.TechnicalTest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicalTestRepository extends MongoRepository<TechnicalTest, String> {
    
    List<TechnicalTest> findByJobId(String jobId);
    
    List<TechnicalTest> findByCandidateId(String candidateId);
    
    List<TechnicalTest> findByCompanyId(String companyId);
    
    Optional<TechnicalTest> findByJobIdAndCandidateId(String jobId, String candidateId);
    
    List<TechnicalTest> findByStatus(TechnicalTest.TestStatus status);
}
