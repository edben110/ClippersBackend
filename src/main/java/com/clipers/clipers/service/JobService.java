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
 * Servicio que implementa Strategy Pattern implícitamente
 * para diferentes algoritmos de matching candidatos ↔ vacantes
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobMatchRepository jobMatchRepository;
    private final NotificationService notificationService;

    @Autowired
    public JobService(JobRepository jobRepository,
                     CompanyRepository companyRepository,
                     UserRepository userRepository,
                     JobMatchRepository jobMatchRepository,
                     NotificationService notificationService) {
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

        Job job = new Job(title, description, location, type, company);
        job.setRequirements(requirements);
        job.setSkills(skills);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);

        job = jobRepository.save(job);

        // Ejecutar matching automático con candidatos
        performAutomaticMatching(job);

        return job;
    }

    /**
     * Strategy Pattern implementado implícitamente
     * Aplica diferentes estrategias de matching según el contexto
     */
    private void performAutomaticMatching(Job job) {
        // En producción, esto se ejecutaría de forma asíncrona
        new Thread(() -> {
            try {
                List<User> candidates = userRepository.findCandidatesWithATSProfile();
                
                for (User candidate : candidates) {
                    // Aplicar múltiples estrategias de matching
                    double overallScore = calculateOverallMatchScore(candidate, job);
                    
                    // Solo crear match si el score es significativo
                    if (overallScore >= 0.3) {
                        String explanation = generateMatchExplanation(candidate, job, overallScore);
                        List<String> matchedSkills = findMatchedSkills(candidate, job);
                        
                        JobMatch jobMatch = new JobMatch(job, candidate, overallScore, explanation);
                        jobMatch.setMatchedSkills(matchedSkills);
                        jobMatchRepository.save(jobMatch);
                        
                        // Notificar al candidato si el match es bueno
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

    // Strategy Pattern - combina múltiples estrategias
    private double calculateOverallMatchScore(User candidate, Job job) {
        double skillScore = calculateSkillMatchScore(candidate, job);
        double experienceScore = calculateExperienceMatchScore(candidate, job);
        double locationScore = calculateLocationMatchScore(candidate, job);
        
        // Pesos para cada estrategia
        double skillWeight = 0.5;
        double experienceWeight = 0.3;
        double locationWeight = 0.2;
        
        return (skillScore * skillWeight) + 
               (experienceScore * experienceWeight) + 
               (locationScore * locationWeight);
    }

    // Estrategia basada en habilidades
    private double calculateSkillMatchScore(User candidate, Job job) {
        if (candidate.getAtsProfile() == null || candidate.getAtsProfile().getSkills().isEmpty()) {
            return 0.0;
        }

        Set<String> candidateSkills = candidate.getAtsProfile().getSkills()
                .stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> jobSkills = job.getSkills()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (jobSkills.isEmpty()) {
            return 0.5; // Score neutro si el trabajo no especifica habilidades
        }

        // Calcular intersección
        Set<String> commonSkills = new HashSet<>(candidateSkills);
        commonSkills.retainAll(jobSkills);

        return (double) commonSkills.size() / jobSkills.size();
    }

    // Estrategia basada en experiencia
    private double calculateExperienceMatchScore(User candidate, Job job) {
        if (candidate.getAtsProfile() == null || candidate.getAtsProfile().getExperience().isEmpty()) {
            return 0.2; // Score bajo si no tiene experiencia registrada
        }

        // Calcular años totales de experiencia
        int totalYearsOfExperience = candidate.getAtsProfile().getExperience()
                .stream()
                .mapToInt(exp -> {
                    LocalDate startDate = exp.getStartDate();
                    LocalDate endDate = exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now();
                    return Period.between(startDate, endDate).getYears();
                })
                .sum();

        // Evaluar experiencia según el tipo de trabajo
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

    // Estrategia basada en ubicación
    private double calculateLocationMatchScore(User candidate, Job job) {
        // Estrategia simple - en producción sería más sofisticada
        if (job.getLocation() == null || job.getLocation().toLowerCase().contains("remoto")) {
            return 1.0; // Trabajo remoto siempre coincide
        }
        
        // Por simplicidad, asumimos coincidencia perfecta o nula
        // En producción se usaría geolocalización
        return 0.7; // Score por defecto para ubicación
    }

    private String generateMatchExplanation(User candidate, Job job, double overallScore) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Análisis de compatibilidad:\n");
        
        double skillScore = calculateSkillMatchScore(candidate, job);
        double experienceScore = calculateExperienceMatchScore(candidate, job);
        
        // Explicación de skills
        if (skillScore >= 0.8) {
            explanation.append("- Excelente coincidencia de habilidades\n");
        } else if (skillScore >= 0.6) {
            explanation.append("- Buena coincidencia de habilidades\n");
        } else if (skillScore >= 0.3) {
            explanation.append("- Coincidencia parcial de habilidades\n");
        } else {
            explanation.append("- Pocas habilidades coincidentes\n");
        }
        
        // Explicación de experiencia
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
        if (candidate.getAtsProfile() == null) {
            return new ArrayList<>();
        }

        Set<String> candidateSkills = candidate.getAtsProfile().getSkills()
                .stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        return job.getSkills()
                .stream()
                .filter(jobSkill -> candidateSkills.contains(jobSkill.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Métodos CRUD estándar
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
        return jobRepository.findAllActiveJobLocations();
    }
}