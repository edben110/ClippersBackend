package com.clipers.clipers.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clipers.clipers.dto.JobDTO;
import com.clipers.clipers.dto.UserDTO;
import com.clipers.clipers.entity.Job;
import com.clipers.clipers.entity.JobMatch;
import com.clipers.clipers.repository.CompanyRepository;
import com.clipers.clipers.service.AuthService;
import com.clipers.clipers.service.JobService;
import com.clipers.clipers.service.NotificationService;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final CompanyRepository companyRepository;
    private final com.clipers.clipers.service.TechnicalTestService technicalTestService;

    @Autowired
    public JobController(JobService jobService, AuthService authService, NotificationService notificationService, CompanyRepository companyRepository, com.clipers.clipers.service.TechnicalTestService technicalTestService) {
        this.jobService = jobService;
        this.authService = authService;
        this.notificationService = notificationService;
        this.companyRepository = companyRepository;
        this.technicalTestService = technicalTestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobDTO> createJob(@RequestBody Map<String, Object> request) {
        try {
            String companyUserId = getCurrentUserId();
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> requirements = (List<String>) request.get("requirements");
            @SuppressWarnings("unchecked")
            List<String> skills = (List<String>) request.get("skills");
            String location = (String) request.get("location");
            String typeStr = (String) request.get("type");
            Integer salaryMin = (Integer) request.get("salaryMin");
            Integer salaryMax = (Integer) request.get("salaryMax");

            Job.JobType type = Job.JobType.valueOf(typeStr.toUpperCase());

            Job job = jobService.createJob(companyUserId, title, description,
                                          requirements, skills, location, type, salaryMin, salaryMax);
            JobDTO jobDTO = new JobDTO(job);
            return ResponseEntity.ok(jobDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear empleo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        return jobService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) List<String> skills) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobsPage;
        
        if (search != null && !search.isEmpty()) {
            jobsPage = jobService.searchActiveJobs(search, pageable);
        } else if (hasFilters(location, type, salaryMin, salaryMax)) {
            Job.JobType jobType = type != null ? Job.JobType.valueOf(type.toUpperCase()) : null;
            jobsPage = jobService.findJobsWithFilters(jobType, location, salaryMin, salaryMax, pageable);
        } else {
            jobsPage = jobService.findActiveJobs(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobs", jobsPage.getContent());
        response.put("hasMore", jobsPage.hasNext());
        response.put("totalPages", jobsPage.getTotalPages());
        response.put("currentPage", page);
        response.put("totalElements", jobsPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getActiveJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobsPage = jobService.findActiveJobs(pageable);

        List<JobDTO> jobDTOs = jobService.convertJobsToDTO(jobsPage.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("jobs", jobDTOs);
        response.put("hasMore", jobsPage.hasNext());
        response.put("totalPages", jobsPage.getTotalPages());
        response.put("currentPage", page);
        response.put("totalElements", jobsPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Job>> getJobsByCompany(@PathVariable String companyId) {
        List<Job> jobs = jobService.findByCompanyId(companyId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<JobDTO>> getMyJobs() {
        try {
            String userId = getCurrentUserId();
            
            // First, find the company associated with this user
            Optional<com.clipers.clipers.entity.Company> companyOpt = 
                companyRepository.findByUserId(userId);
            
            if (!companyOpt.isPresent()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            String companyId = companyOpt.get().getId();
            
            // Now find jobs by company ID
            List<Job> jobs = jobService.findByCompanyId(companyId);
            List<JobDTO> jobDTOs = jobService.convertJobsToDTO(jobs);
            return ResponseEntity.ok(jobDTOs);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener tus empleos: " + e.getMessage(), e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Job>> searchJobs(
            @RequestParam String query, Pageable pageable) {
        Page<Job> jobs = jobService.searchActiveJobs(query, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Job>> filterJobs(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            Pageable pageable) {
        
        Job.JobType jobType = type != null ? Job.JobType.valueOf(type.toUpperCase()) : null;
        Page<Job> jobs = jobService.findJobsWithFilters(jobType, location, minSalary, maxSalary, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/by-skill")
    public ResponseEntity<List<Job>> getJobsBySkill(@RequestParam String skill) {
        List<Job> jobs = jobService.findBySkill(skill);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Job> updateJob(
            @PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> requirements = (List<String>) request.get("requirements");
            @SuppressWarnings("unchecked")
            List<String> skills = (List<String>) request.get("skills");
            String location = (String) request.get("location");
            String typeStr = (String) request.get("type");
            Integer salaryMin = (Integer) request.get("salaryMin");
            Integer salaryMax = (Integer) request.get("salaryMax");

            Job.JobType type = Job.JobType.valueOf(typeStr.toUpperCase());

            Job updatedJob = jobService.updateJob(id, title, description, 
                                                requirements, skills, location, type, salaryMin, salaryMax);
            return ResponseEntity.ok(updatedJob);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar empleo: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deactivateJob(@PathVariable String id) {
        try {
            jobService.deactivateJob(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al desactivar empleo: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deleteJob(@PathVariable String id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar empleo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/matches/user/{userId}")
    public ResponseEntity<List<JobMatch>> getMatchesForUser(@PathVariable String userId) {
        List<JobMatch> matches = jobService.getMatchesForUser(userId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{jobId}/matches")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<JobMatch>> getMatchesForJob(@PathVariable String jobId) {
        List<JobMatch> matches = jobService.getMatchesForJob(jobId);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<com.clipers.clipers.dto.JobApplicationDTO> applyToJob(@PathVariable String jobId, @RequestBody(required = false) Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String applicationMessage = request != null ? request.get("message") : null;

            JobMatch application = jobService.applyToJob(jobId, userId, applicationMessage);
            List<com.clipers.clipers.dto.JobApplicationDTO> dtos = jobService.convertApplicationsToDTO(List.of(application));
            
            return ResponseEntity.ok(dtos.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error al aplicar al trabajo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getJobLocations() {
        List<String> locations = jobService.getAllJobLocations();
        return ResponseEntity.ok(locations);
    }

    @PostMapping("/populate-sample-jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Map<String, Object>> populateSampleJobs() {
        try {
            String companyUserId = getCurrentUserId();
            int createdCount = jobService.populateSampleJobs(companyUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Empleos de ejemplo creados correctamente");
            response.put("createdCount", createdCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear empleos de ejemplo: " + e.getMessage(), e);
        }
    }

    // Endpoints for application management

    @GetMapping("/my-applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<com.clipers.clipers.dto.JobApplicationDTO>> getMyApplications() {
        try {
            String userId = getCurrentUserId();
            List<JobMatch> applications = jobService.getUserApplications(userId);
            List<com.clipers.clipers.dto.JobApplicationDTO> applicationDTOs = jobService.convertApplicationsToDTO(applications);
            return ResponseEntity.ok(applicationDTOs);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener tus aplicaciones: " + e.getMessage(), e);
        }
    }

    @PutMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<com.clipers.clipers.dto.JobApplicationDTO> updateApplicationStatus(
            @PathVariable String applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String companyUserId = getCurrentUserId();
            String statusStr = request.get("status");
            JobMatch.ApplicationStatus status = JobMatch.ApplicationStatus.valueOf(statusStr.toUpperCase());

            JobMatch updatedApplication = jobService.updateApplicationStatus(applicationId, status, companyUserId);
            List<com.clipers.clipers.dto.JobApplicationDTO> dtos = jobService.convertApplicationsToDTO(List.of(updatedApplication));
            
            return ResponseEntity.ok(dtos.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar estado de aplicación: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<com.clipers.clipers.dto.JobApplicationDTO>> getJobApplications(@PathVariable String jobId) {
        try {
            String companyUserId = getCurrentUserId();
            List<JobMatch> applications = jobService.getJobApplications(jobId, companyUserId);
            List<com.clipers.clipers.dto.JobApplicationDTO> applicationDTOs = jobService.convertApplicationsToDTO(applications);
            return ResponseEntity.ok(applicationDTOs);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener aplicaciones del trabajo: " + e.getMessage(), e);
        }
    }

    private String getCurrentUserId() {
        try {
            UserDTO currentUser = authService.getCurrentUser();
            return currentUser.getId();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener usuario actual: " + e.getMessage(), e);
        }
    }

    private boolean hasFilters(String location, String type, Integer salaryMin, Integer salaryMax) {
        return (location != null && !location.isEmpty()) ||
               (type != null && !type.isEmpty()) ||
               salaryMin != null ||
               salaryMax != null;
    }

    // Technical Test Endpoints
    
    @PostMapping("/{jobId}/technical-test/send")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<com.clipers.clipers.entity.TechnicalTest> sendTechnicalTest(
            @PathVariable String jobId,
            @RequestBody Map<String, String> request) {
        try {
            String candidateId = request.get("candidateId");
            String companyUserId = getCurrentUserId();
            
            // Get company ID
            Optional<com.clipers.clipers.entity.Company> companyOpt = 
                companyRepository.findByUserId(companyUserId);
            
            if (!companyOpt.isPresent()) {
                throw new RuntimeException("Company not found");
            }
            
            String companyId = companyOpt.get().getId();
            
            com.clipers.clipers.entity.TechnicalTest test = 
                technicalTestService.generateAndSendTest(jobId, candidateId, companyId);
            
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            throw new RuntimeException("Error sending technical test: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{jobId}/technical-tests")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<com.clipers.clipers.entity.TechnicalTest>> getJobTechnicalTests(
            @PathVariable String jobId) {
        try {
            List<com.clipers.clipers.entity.TechnicalTest> tests = 
                technicalTestService.getTestsByJob(jobId);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            throw new RuntimeException("Error getting technical tests: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/technical-tests/my-tests")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<com.clipers.clipers.entity.TechnicalTest>> getMyCandidateTests() {
        try {
            String candidateId = getCurrentUserId();
            List<com.clipers.clipers.entity.TechnicalTest> tests = 
                technicalTestService.getTestsByCandidate(candidateId);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            throw new RuntimeException("Error getting your technical tests: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/technical-tests/{testId}/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<com.clipers.clipers.entity.TechnicalTest> submitTechnicalTest(
            @PathVariable String testId,
            @RequestBody Map<String, String> request) {
        try {
            String response = request.get("response");
            com.clipers.clipers.entity.TechnicalTest test = 
                technicalTestService.submitTest(testId, response);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            throw new RuntimeException("Error submitting response: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/technical-tests/{testId}/review")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<com.clipers.clipers.entity.TechnicalTest> reviewTechnicalTest(
            @PathVariable String testId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer score = (Integer) request.get("score");
            String feedback = (String) request.get("feedback");
            com.clipers.clipers.entity.TechnicalTest test = 
                technicalTestService.reviewTest(testId, score, feedback);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            throw new RuntimeException("Error reviewing test: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{jobId}/technical-tests/candidate/{candidateId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<com.clipers.clipers.entity.TechnicalTest>> getCandidateTechnicalTests(
            @PathVariable String jobId,
            @PathVariable String candidateId) {
        try {
            List<com.clipers.clipers.entity.TechnicalTest> tests = 
                technicalTestService.getTestsByJobAndCandidate(jobId, candidateId);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            throw new RuntimeException("Error getting candidate technical tests: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/technical-tests/{testId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deleteTechnicalTest(@PathVariable String testId) {
        try {
            technicalTestService.deleteTest(testId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting technical test: " + e.getMessage(), e);
        }
    }
}
