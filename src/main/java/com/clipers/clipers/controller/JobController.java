package com.clipers.clipers.controller;

import com.clipers.clipers.dto.UserDTO;
import com.clipers.clipers.dto.JobDTO;
import com.clipers.clipers.entity.Job;
import com.clipers.clipers.entity.JobMatch;
import com.clipers.clipers.service.AuthService;
import com.clipers.clipers.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;
    private final AuthService authService;

    @Autowired
    public JobController(JobService jobService, AuthService authService) {
        this.jobService = jobService;
        this.authService = authService;
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

        List<JobDTO> jobDTOs = jobsPage.getContent().stream()
                .map(JobDTO::new)
                .collect(Collectors.toList());

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
    public ResponseEntity<Void> applyToJob(@PathVariable String jobId) {
        try {
            String userId = getCurrentUserId();
            // En producción, aquí se crearía una aplicación al trabajo
            // Por ahora solo simulamos
            System.out.println("Usuario " + userId + " aplicó al trabajo " + jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al aplicar al trabajo: " + e.getMessage(), e);
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<List<String>> getJobLocations() {
        List<String> locations = jobService.getAllJobLocations();
        return ResponseEntity.ok(locations);
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
}
