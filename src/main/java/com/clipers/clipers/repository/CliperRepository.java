package com.clipers.clipers.repository;

import com.clipers.clipers.entity.Cliper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CliperRepository extends JpaRepository<Cliper, String> {
    
    List<Cliper> findByUserId(String userId);
    
    Page<Cliper> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Cliper> findByStatus(Cliper.Status status);
    
    Page<Cliper> findByStatusOrderByCreatedAtDesc(Cliper.Status status, Pageable pageable);
    
    @Query("SELECT c FROM Cliper c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Cliper> searchClipers(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c FROM Cliper c JOIN c.skills s WHERE " +
           "LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Cliper> findBySkillsContaining(@Param("skill") String skill);
    
    @Query("SELECT c FROM Cliper c WHERE c.status = 'DONE' ORDER BY c.createdAt DESC")
    Page<Cliper> findProcessedClipersOrderByCreatedAtDesc(Pageable pageable);
}
