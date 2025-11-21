package com.clipers.clipers.service;

import com.clipers.clipers.entity.*;
import com.clipers.clipers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that implements Strategy Pattern implicitly
 * for different candidate-job matching algorithms
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobMatchRepository jobMatchRepository;
    private final ATSProfileRepository atsProfileRepository;
    private final NotificationService notificationService;

    @Autowired
    public JobService(JobRepository jobRepository,
                     CompanyRepository companyRepository,
                     UserRepository userRepository,
                     JobMatchRepository jobMatchRepository,
                     ATSProfileRepository atsProfileRepository,
                     NotificationService notificationService) {
        this.atsProfileRepository = atsProfileRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.notificationService = notificationService;
    }

    public Job createJob(String companyUserId, String title, String description, 
                        List<String> requirements, List<String> skills, 
                        String location, Job.JobType type, Integer salaryMin, Integer salaryMax) {
        
        Company company = companyRepository.findByUserId(companyUserId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Job job = new Job(title, description, location, type, company.getId());
        job.setRequirements(requirements);
        job.setSkills(skills);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);

        job = jobRepository.save(job);

        // Execute automatic matching with candidates
        performAutomaticMatching(job);

        return job;
    }

    /**
     * Strategy Pattern implemented implicitly
     * Applies different matching strategies based on context
     */
    private void performAutomaticMatching(Job job) {
        // In production, this would run asynchronously
        new Thread(() -> {
            try {
                List<User> candidates = userRepository.findCandidatesWithATSProfile();
                
                for (User candidate : candidates) {
                    // Apply multiple matching strategies
                    double overallScore = calculateOverallMatchScore(candidate, job);
                    
                    // Only create match if score is significant
                    if (overallScore >= 0.3) {
                        String explanation = generateMatchExplanation(candidate, job, overallScore);
                        List<String> matchedSkills = findMatchedSkills(candidate, job);
                        
                        JobMatch jobMatch = new JobMatch(job.getId(), candidate.getId(), overallScore, explanation);
                        jobMatch.setMatchedSkills(matchedSkills);
                        jobMatchRepository.save(jobMatch);
                        
                        // Notify candidate if match is good
                        if (overallScore >= 0.6) {
                            notificationService.notifyJobMatched(candidate.getId(), job.getId(), overallScore);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en matching automático para job " + job.getId() + ": " + e.getMessage());
            }
        }).start();
    }

    // Strategy Pattern - combines multiple strategies
    private double calculateOverallMatchScore(User candidate, Job job) {
        double skillScore = calculateSkillMatchScore(candidate, job);
        double experienceScore = calculateExperienceMatchScore(candidate, job);
        double locationScore = calculateLocationMatchScore(candidate, job);
        
        // Weights for each strategy
        double skillWeight = 0.5;
        double experienceWeight = 0.3;
        double locationWeight = 0.2;
        
        return (skillScore * skillWeight) + 
               (experienceScore * experienceWeight) + 
               (locationScore * locationWeight);
    }

    // Skill-based strategy
    private double calculateSkillMatchScore(User candidate, Job job) {
        Optional<ATSProfile> atsProfileOpt = atsProfileRepository.findByUserId(candidate.getId());
        if (atsProfileOpt.isEmpty() || atsProfileOpt.get().getSkills().isEmpty()) {
            return 0.0;
        }

        ATSProfile atsProfile = atsProfileOpt.get();
        Set<String> candidateSkills = atsProfile.getSkills()
                .stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> jobSkills = job.getSkills()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (jobSkills.isEmpty()) {
            return 0.5; // Neutral score if job doesn't specify skills
        }

        // Calculate intersection
        Set<String> commonSkills = new HashSet<>(candidateSkills);
        commonSkills.retainAll(jobSkills);

        return (double) commonSkills.size() / jobSkills.size();
    }

    // Experience-based strategy
    private double calculateExperienceMatchScore(User candidate, Job job) {
        Optional<ATSProfile> atsProfileOpt = atsProfileRepository.findByUserId(candidate.getId());
        if (atsProfileOpt.isEmpty() || atsProfileOpt.get().getExperience().isEmpty()) {
            return 0.2; // Low score if no experience registered
        }

        ATSProfile atsProfile = atsProfileOpt.get();
        // Calculate total years of experience
        int totalYearsOfExperience = atsProfile.getExperience()
                .stream()
                .mapToInt(exp -> {
                    try {
                        if (exp.getStartDate() == null) return 0;
                        // Parse YYYY-MM format to LocalDate (use first day of month)
                        LocalDate startDate = LocalDate.parse(exp.getStartDate() + "-01");
                        LocalDate endDate = exp.getEndDate() != null ? 
                            LocalDate.parse(exp.getEndDate() + "-01") : LocalDate.now();
                        return Period.between(startDate, endDate).getYears();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        // Evaluate experience based on job type
        return switch (job.getType()) {
            case INTERNSHIP -> totalYearsOfExperience >= 0 ? 0.9 : 0.5;
            case FULL_TIME -> {
                if (totalYearsOfExperience >= 5) yield 0.9;
                else if (totalYearsOfExperience >= 2) yield 0.7;
                else if (totalYearsOfExperience >= 1) yield 0.5;
                else yield 0.3;
            }
            case PART_TIME, CONTRACT -> totalYearsOfExperience >= 1 ? 0.8 : 0.6;
        };
    }

    // Location-based strategy
    private double calculateLocationMatchScore(User candidate, Job job) {
        // Simple strategy - in production would be more sophisticated
        if (job.getLocation() == null || job.getLocation().toLowerCase().contains("remoto")) {
            return 1.0; // Remote work always matches
        }
        
        // For simplicity, we assume perfect or no match
        // In production would use geolocation
        return 0.7; // Default score for location
    }

    private String generateMatchExplanation(User candidate, Job job, double overallScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Análisis de compatibilidad:\n");
        
        double skillScore = calculateSkillMatchScore(candidate, job);
        double experienceScore = calculateExperienceMatchScore(candidate, job);
        
        // Skills explanation
        if (skillScore >= 0.8) {
            explanation.append("- Excelente coincidencia de habilidades\n");
        } else if (skillScore >= 0.6) {
            explanation.append("- Buena coincidencia de habilidades\n");
        } else if (skillScore >= 0.3) {
            explanation.append("- Coincidencia parcial de habilidades\n");
        } else {
            explanation.append("- Pocas habilidades coincidentes\n");
        }
        
        // Experience explanation
        if (experienceScore >= 0.8) {
            explanation.append("- Experiencia muy adecuada para el puesto\n");
        } else if (experienceScore >= 0.6) {
            explanation.append("- Experiencia adecuada para el puesto\n");
        } else {
            explanation.append("- Experiencia limitada para el puesto\n");
        }
        
        explanation.append(String.format("Score general: %.2f", overallScore));
        return explanation.toString();
    }

    private List<String> findMatchedSkills(User candidate, Job job) {
        Optional<ATSProfile> atsProfileOpt = atsProfileRepository.findByUserId(candidate.getId());
        if (atsProfileOpt.isEmpty()) {
            return new ArrayList<>();
        }

        ATSProfile atsProfile = atsProfileOpt.get();
        Set<String> candidateSkills = atsProfile.getSkills()
                .stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        return job.getSkills()
                .stream()
                .filter(jobSkill -> candidateSkills.contains(jobSkill.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Standard CRUD methods
    public Optional<Job> findById(String id) {
        return jobRepository.findById(id);
    }

    public Page<Job> findActiveJobs(Pageable pageable) {
        return jobRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }

    public List<Job> findByCompanyId(String companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    public Page<Job> searchActiveJobs(String query, Pageable pageable) {
        return jobRepository.searchActiveJobs(query, pageable);
    }

    public Page<Job> findJobsWithFilters(Job.JobType type, String location, 
                                        Integer minSalary, Integer maxSalary, Pageable pageable) {
        return jobRepository.findJobsWithFilters(type, location, minSalary, maxSalary, pageable);
    }

    public List<Job> findBySkill(String skill) {
        return jobRepository.findActiveJobsBySkill(skill);
    }

    public Job updateJob(String jobId, String title, String description, 
                        List<String> requirements, List<String> skills,
                        String location, Job.JobType type, Integer salaryMin, Integer salaryMax) {
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        job.setTitle(title);
        job.setDescription(description);
        job.setRequirements(requirements);
        job.setSkills(skills);
        job.setLocation(location);
        job.setType(type);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);

        return jobRepository.save(job);
    }

    public void deactivateJob(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));
        
        job.setIsActive(false);
        jobRepository.save(job);
    }

    public void deleteJob(String jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new RuntimeException("Empleo no encontrado");
        }
        jobRepository.deleteById(jobId);
    }

    public List<JobMatch> getMatchesForUser(String userId) {
        return jobMatchRepository.findByUserId(userId);
    }

    public List<JobMatch> getMatchesForJob(String jobId) {
        return jobMatchRepository.findByJobId(jobId);
    }

    public List<String> getAllJobLocations() {
        return jobRepository.findAllActiveJobLocations()
                .stream()
                .map(Job::getLocation)
                .distinct()
                .collect(Collectors.toList());
    }

    public int populateSampleJobs(String companyUserId) {
        Company company = companyRepository.findByUserId(companyUserId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        List<Job> sampleJobs = new ArrayList<>();

        // Job 1: Desarrollador Full Stack
        Job job1 = new Job("Desarrollador Full Stack", 
            "Buscamos un desarrollador full stack con experiencia en React y Node.js para unirse a nuestro equipo de desarrollo de productos.",
            "Bogotá, Colombia", Job.JobType.FULL_TIME, company.getId());
        job1.setRequirements(Arrays.asList(
            "3+ años de experiencia en desarrollo web",
            "Dominio de JavaScript/TypeScript",
            "Experiencia con bases de datos SQL y NoSQL"
        ));
        job1.setSkills(Arrays.asList("React", "Node.js", "TypeScript", "MongoDB", "PostgreSQL"));
        job1.setSalaryMin(4000000);
        job1.setSalaryMax(6000000);
        sampleJobs.add(job1);

        // Job 2: UX/UI Designer
        Job job2 = new Job("Diseñador UX/UI Senior",
            "Únete a nuestro equipo creativo para diseñar experiencias de usuario excepcionales para nuestros productos digitales.",
            "Medellín, Colombia", Job.JobType.FULL_TIME, company.getId());
        job2.setRequirements(Arrays.asList(
            "5+ años de experiencia en diseño UX/UI",
            "Portfolio sólido con casos de estudio",
            "Experiencia con Figma y Adobe Creative Suite"
        ));
        job2.setSkills(Arrays.asList("Figma", "Adobe XD", "Sketch", "Prototyping", "User Research"));
        job2.setSalaryMin(3500000);
        job2.setSalaryMax(5500000);
        sampleJobs.add(job2);

        // Job 3: Data Scientist
        Job job3 = new Job("Data Scientist",
            "Buscamos un científico de datos para analizar grandes volúmenes de información y generar insights accionables.",
            "Remoto", Job.JobType.FULL_TIME, company.getId());
        job3.setRequirements(Arrays.asList(
            "Maestría en Ciencias de la Computación, Estadística o campo relacionado",
            "Experiencia con Python y R",
            "Conocimiento de machine learning y deep learning"
        ));
        job3.setSkills(Arrays.asList("Python", "R", "TensorFlow", "Pandas", "SQL", "Machine Learning"));
        job3.setSalaryMin(5000000);
        job3.setSalaryMax(8000000);
        sampleJobs.add(job3);

        // Job 4: DevOps Engineer
        Job job4 = new Job("Ingeniero DevOps",
            "Buscamos un ingeniero DevOps para optimizar nuestros procesos de CI/CD y gestionar nuestra infraestructura en la nube.",
            "Cali, Colombia", Job.JobType.FULL_TIME, company.getId());
        job4.setRequirements(Arrays.asList(
            "3+ años de experiencia en DevOps",
            "Experiencia con AWS o Azure",
            "Conocimiento de Docker y Kubernetes"
        ));
        job4.setSkills(Arrays.asList("AWS", "Docker", "Kubernetes", "Jenkins", "Terraform", "Linux"));
        job4.setSalaryMin(4500000);
        job4.setSalaryMax(7000000);
        sampleJobs.add(job4);

        // Job 5: Product Manager
        Job job5 = new Job("Product Manager",
            "Únete como Product Manager para liderar el desarrollo de nuestros productos digitales y definir la estrategia de producto.",
            "Bogotá, Colombia", Job.JobType.FULL_TIME, company.getId());
        job5.setRequirements(Arrays.asList(
            "4+ años de experiencia como Product Manager",
            "Experiencia en metodologías ágiles",
            "Habilidades de liderazgo y comunicación"
        ));
        job5.setSkills(Arrays.asList("Product Management", "Agile", "Scrum", "Jira", "Analytics"));
        job5.setSalaryMin(5000000);
        job5.setSalaryMax(7500000);
        sampleJobs.add(job5);

        // Job 6: Mobile Developer
        Job job6 = new Job("Desarrollador Mobile (React Native)",
            "Desarrolla aplicaciones móviles innovadoras usando React Native para iOS y Android.",
            "Remoto", Job.JobType.FULL_TIME, company.getId());
        job6.setRequirements(Arrays.asList(
            "2+ años de experiencia con React Native",
            "Conocimiento de iOS y Android",
            "Experiencia publicando apps en stores"
        ));
        job6.setSkills(Arrays.asList("React Native", "JavaScript", "iOS", "Android", "Redux"));
        job6.setSalaryMin(3500000);
        job6.setSalaryMax(5500000);
        sampleJobs.add(job6);

        // Job 7: QA Automation Engineer
        Job job7 = new Job("Ingeniero QA Automation",
            "Únete a nuestro equipo de calidad para automatizar pruebas y asegurar la excelencia de nuestros productos.",
            "Medellín, Colombia", Job.JobType.FULL_TIME, company.getId());
        job7.setRequirements(Arrays.asList(
            "3+ años de experiencia en QA Automation",
            "Experiencia con Selenium o Cypress",
            "Conocimiento de CI/CD"
        ));
        job7.setSkills(Arrays.asList("Selenium", "Cypress", "Java", "JavaScript", "TestNG", "JUnit"));
        job7.setSalaryMin(3000000);
        job7.setSalaryMax(5000000);
        sampleJobs.add(job7);

        // Job 8: Backend Developer
        Job job8 = new Job("Desarrollador Backend Java",
            "Desarrolla servicios backend robustos y escalables usando Java y Spring Boot.",
            "Bogotá, Colombia", Job.JobType.FULL_TIME, company.getId());
        job8.setRequirements(Arrays.asList(
            "4+ años de experiencia con Java",
            "Experiencia con Spring Boot",
            "Conocimiento de microservicios"
        ));
        job8.setSkills(Arrays.asList("Java", "Spring Boot", "Microservices", "REST API", "MySQL"));
        job8.setSalaryMin(4000000);
        job8.setSalaryMax(6500000);
        sampleJobs.add(job8);

        // Save all jobs
        jobRepository.saveAll(sampleJobs);

        // Execute automatic matching for each job
        sampleJobs.forEach(this::performAutomaticMatching);

        return sampleJobs.size();
    }

    public JobMatch applyToJob(String jobId, String userId, String applicationMessage) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        if (!job.getIsActive()) {
            throw new RuntimeException("Este trabajo ya no está activo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!User.Role.CANDIDATE.equals(user.getRole())) {
            throw new RuntimeException("Solo los candidatos pueden aplicar a trabajos");
        }

        // Check if already applied
        Optional<JobMatch> existingApplication = jobMatchRepository.findByUserIdAndJobId(userId, jobId);
        if (existingApplication.isPresent()) {
            throw new RuntimeException("Ya has aplicado a este trabajo");
        }

        // Create new application
        JobMatch application = new JobMatch(job.getId(), user.getId(), 0.0, "Manual candidate application");
        application.setStatus(JobMatch.ApplicationStatus.PENDING);
        application.setApplicationMessage(applicationMessage);

        return jobMatchRepository.save(application);
    }

    public JobMatch updateApplicationStatus(String jobMatchId, JobMatch.ApplicationStatus status, String companyUserId) {
        JobMatch jobMatch = jobMatchRepository.findById(jobMatchId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        // Verify that the company owns the job
        Job job = jobRepository.findById(jobMatch.getJobId())
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));
        Company company = companyRepository.findById(job.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        if (!company.getUserId().equals(companyUserId)) {
            throw new RuntimeException("No tienes permisos para gestionar esta aplicación");
        }

        jobMatch.setStatus(status);
        JobMatch updated = jobMatchRepository.save(jobMatch);

        // Notify candidate
        String message = switch (status) {
            case ACCEPTED -> "¡Felicitaciones! Tu aplicación ha sido aceptada.";
            case REJECTED -> "Tu aplicación ha sido rechazada.";
            case PENDING -> "El estado de tu aplicación ha cambiado a pendiente.";
        };
        notificationService.notifyApplicationStatusUpdate(jobMatch.getUserId(), jobMatch.getJobId(), status);

        return updated;
    }

    public List<JobMatch> getUserApplications(String userId) {
        return jobMatchRepository.findByUserId(userId);
    }

    public List<JobMatch> getJobApplications(String jobId, String companyUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Trabajo no encontrado"));

        Company company = companyRepository.findById(job.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        if (!company.getUserId().equals(companyUserId)) {
            throw new RuntimeException("No tienes permisos para ver las aplicaciones de este trabajo");
        }

        return jobMatchRepository.findByJobId(jobId);
    }

    // Helper methods for DTOs
    public List<com.clipers.clipers.dto.JobDTO> convertJobsToDTO(List<Job> jobs) {
        return jobs.stream()
                .map(job -> {
                    Company company = companyRepository.findById(job.getCompanyId()).orElse(null);
                    return new com.clipers.clipers.dto.JobDTO(job, company);
                })
                .collect(Collectors.toList());
    }

    public List<com.clipers.clipers.dto.JobApplicationDTO> convertApplicationsToDTO(List<JobMatch> applications) {
        return applications.stream()
                .map(app -> {
                    com.clipers.clipers.dto.JobApplicationDTO dto = new com.clipers.clipers.dto.JobApplicationDTO(app);
                    
                    // Populate job info
                    jobRepository.findById(app.getJobId()).ifPresent(job -> {
                        Company company = companyRepository.findById(job.getCompanyId()).orElse(null);
                        dto.setJob(new com.clipers.clipers.dto.JobDTO(job, company));
                    });
                    
                    // Populate user info
                    userRepository.findById(app.getUserId()).ifPresent(user -> {
                        dto.setUser(new com.clipers.clipers.dto.UserDTO(user));
                        
                        // Populate ATS profile
                        atsProfileRepository.findByUserId(user.getId()).ifPresent(atsProfile -> {
                            dto.setAtsProfile(new com.clipers.clipers.dto.ATSProfileDTO(atsProfile));
                        });
                    });
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
}