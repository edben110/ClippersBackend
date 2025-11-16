package com.clipers.clipers.controller;

import com.clipers.clipers.dto.matching.*;
import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.entity.Job;
import com.clipers.clipers.entity.JobMatch;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.ATSProfileRepository;
import com.clipers.clipers.repository.JobMatchRepository;
import com.clipers.clipers.repository.JobRepository;
import com.clipers.clipers.repository.UserRepository;
import com.clipers.clipers.service.AIMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final AIMatchingService aiMatchingService;
    private final UserRepository userRepository;
    private final ATSProfileRepository atsProfileRepository;
    private final JobRepository jobRepository;
    private final JobMatchRepository jobMatchRepository;

    public AIController(
            AIMatchingService aiMatchingService,
            UserRepository userRepository,
            ATSProfileRepository atsProfileRepository,
            JobRepository jobRepository,
            JobMatchRepository jobMatchRepository
    ) {
        this.aiMatchingService = aiMatchingService;
        this.userRepository = userRepository;
        this.atsProfileRepository = atsProfileRepository;
        this.jobRepository = jobRepository;
        this.jobMatchRepository = jobMatchRepository;
    }

    /**
     * Health check for AI service
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponseDTO> checkHealth() {
        logger.info("Checking AI service health");
        HealthResponseDTO health = aiMatchingService.checkHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Match a single candidate against a job
     */
    @PostMapping("/match/single")
    @PreAuthorize("hasAnyRole('COMPANY', 'CANDIDATE')")
    public ResponseEntity<SingleMatchResponseDTO> matchSingle(@RequestBody BatchMatchRequestDTO request) {
        logger.info("Matching single candidate: candidateId={}, jobId={}", 
                   request.getCandidates().get(0).getId(), request.getJob().getId());

        if (request.getCandidates() == null || request.getCandidates().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        CandidateDTO candidate = request.getCandidates().get(0);
        SingleMatchResponseDTO result = aiMatchingService.matchSingleCandidate(candidate, request.getJob());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Match multiple candidates against a job (returns ranked list)
     */
    @PostMapping("/match/batch")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<BatchMatchResponseDTO> matchBatch(@RequestBody BatchMatchRequestDTO request) {
        logger.info("Matching batch: {} candidates, jobId={}", 
                   request.getCandidates().size(), request.getJob().getId());

        if (request.getCandidates() == null || request.getCandidates().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getCandidates().size() > 100) {
            return ResponseEntity.badRequest().build();
        }

        BatchMatchResponseDTO result = aiMatchingService.matchBatchCandidates(
            request.getCandidates(), 
            request.getJob()
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed explanation of a match
     */
    @PostMapping("/match/explain")
    @PreAuthorize("hasAnyRole('COMPANY', 'CANDIDATE')")
    public ResponseEntity<ExplainMatchResponseDTO> explainMatch(@RequestBody ExplainMatchRequestDTO request) {
        logger.info("Explaining match: candidateId={}, jobId={}", 
                   request.getCandidate().getId(), request.getJob().getId());

        ExplainMatchResponseDTO result = aiMatchingService.explainMatch(
            request.getCandidate(), 
            request.getJob(), 
            request.getIncludeSuggestions() != null ? request.getIncludeSuggestions() : true
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Match a candidate (by userId) against a job (by jobId)
     * Convenience endpoint that fetches candidate and job data from database
     */
    @PostMapping("/match/user/{userId}/job/{jobId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'CANDIDATE')")
    public ResponseEntity<SingleMatchResponseDTO> matchUserToJob(
            @PathVariable String userId,
            @PathVariable String jobId
    ) {
        logger.info("Matching user {} to job {}", userId, jobId);

        // Fetch candidate data
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("ATS Profile not found"));

        // Fetch job data
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Convert to DTOs
        CandidateDTO candidateDTO = convertToCandidateDTO(user, profile);
        JobDTO jobDTO = convertToJobDTO(job);

        // Perform matching
        SingleMatchResponseDTO result = aiMatchingService.matchSingleCandidate(candidateDTO, jobDTO);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Find best candidates for a job
     * Fetches all applicants for the job and ranks them using AI
     */
    @GetMapping("/match/job/{jobId}/candidates")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<BatchMatchResponseDTO> findBestCandidates(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        logger.info("Finding best candidates for job {}", jobId);

        // Fetch job
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Fetch all job matches (applications) for this job
        List<JobMatch> jobMatches = jobMatchRepository.findByJobId(jobId);
        
        logger.info("Found {} applications for job {}", jobMatches.size(), jobId);
        
        if (jobMatches.isEmpty()) {
            logger.warn("No applications found for job {}", jobId);
            return ResponseEntity.noContent().build();
        }

        // Get user IDs from job matches and fetch users with their profiles
        List<CandidateDTO> candidateDTOs = jobMatches.stream()
            .limit(Math.min(limit, 100)) // Max 100 candidates
            .map(jobMatch -> {
                try {
                    User user = userRepository.findById(jobMatch.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found: " + jobMatch.getUserId()));
                    
                    ATSProfile profile = atsProfileRepository.findByUserId(user.getId())
                        .orElse(null); // Allow null profile, will have lower AI score
                    
                    return convertToCandidateDTO(user, profile);
                } catch (Exception e) {
                    logger.error("Error fetching candidate data for user {}: {}", jobMatch.getUserId(), e.getMessage());
                    return null;
                }
            })
            .filter(dto -> dto != null) // Remove failed conversions
            .collect(Collectors.toList());
        
        if (candidateDTOs.isEmpty()) {
            logger.warn("No valid candidate DTOs created for job {}", jobId);
            return ResponseEntity.noContent().build();
        }

        JobDTO jobDTO = convertToJobDTO(job);

        // Perform batch matching
        logger.info("Performing AI matching for {} candidates", candidateDTOs.size());
        BatchMatchResponseDTO result = aiMatchingService.matchBatchCandidates(candidateDTOs, jobDTO);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get match explanation for user and job
     */
    @GetMapping("/match/user/{userId}/job/{jobId}/explain")
    @PreAuthorize("hasAnyRole('COMPANY', 'CANDIDATE')")
    public ResponseEntity<ExplainMatchResponseDTO> explainUserJobMatch(
            @PathVariable String userId,
            @PathVariable String jobId,
            @RequestParam(defaultValue = "true") boolean includeSuggestions
    ) {
        logger.info("Explaining match for user {} and job {}", userId, jobId);

        // Fetch data
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        ATSProfile profile = atsProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("ATS Profile not found"));

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        // Convert to DTOs
        CandidateDTO candidateDTO = convertToCandidateDTO(user, profile);
        JobDTO jobDTO = convertToJobDTO(job);

        // Get explanation
        ExplainMatchResponseDTO result = aiMatchingService.explainMatch(
            candidateDTO, 
            jobDTO, 
            includeSuggestions
        );
        
        return ResponseEntity.ok(result);
    }

    // Helper methods to convert entities to DTOs

    private CandidateDTO convertToCandidateDTO(User user, ATSProfile profile) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(user.getId());
        dto.setName(user.getFirstName() + " " + user.getLastName());
        dto.setLocation(user.getAddress());
        
        if (profile != null) {
            // Skills
            dto.setSkills(profile.getSkills() != null ? 
                profile.getSkills().stream()
                    .map(s -> s.getName())
                    .collect(Collectors.toList()) : 
                new ArrayList<>()
            );
            
            // Calculate experience years from experience list
            // Since dates are strings in format YYYY-MM, we'll set a default value
            double totalYears = 0.0;
            if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
                // Simple estimation: count number of experiences
                totalYears = profile.getExperience().size() * 2.0; // Assume 2 years per experience average
            }
            dto.setExperienceYears(totalYears);
            
            // Experience details
            dto.setExperience(profile.getExperience() != null ?
                profile.getExperience().stream()
                    .map(exp -> new ExperienceDTO(
                        exp.getCompany(),
                        exp.getPosition(),
                        exp.getDescription(),
                        null, // startDate
                        null, // endDate
                        null  // years
                    ))
                    .collect(Collectors.toList()) :
                new ArrayList<>()
            );
            
            // Education
            dto.setEducation(profile.getEducation() != null ?
                profile.getEducation().stream()
                    .map(edu -> new EducationDTO(
                        edu.getDegree(),
                        edu.getInstitution(),
                        edu.getField(),
                        null, // startYear
                        null  // endYear
                    ))
                    .collect(Collectors.toList()) :
                new ArrayList<>()
            );
            
            // Languages
            dto.setLanguages(profile.getLanguages() != null ?
                profile.getLanguages().stream()
                    .map(lang -> lang.getName())
                    .collect(Collectors.toList()) :
                new ArrayList<>()
            );
            
            // Summary
            dto.setSummary(profile.getSummary());
        } else {
            dto.setSkills(new ArrayList<>());
            dto.setExperienceYears(0.0);
            dto.setExperience(new ArrayList<>());
            dto.setEducation(new ArrayList<>());
            dto.setLanguages(new ArrayList<>());
        }
        
        return dto;
    }

    private JobDTO convertToJobDTO(Job job) {
        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setSkills(job.getSkills() != null ? job.getSkills() : new ArrayList<>());
        dto.setRequirements(job.getRequirements() != null ? job.getRequirements() : new ArrayList<>());
        dto.setLocation(job.getLocation());
        dto.setType(job.getType() != null ? job.getType().toString() : "FULL_TIME");
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setMinExperienceYears(null); // No existe en la entidad Job
        
        return dto;
    }
}
