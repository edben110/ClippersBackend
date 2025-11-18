package com.clipers.clipers.service;

import com.clipers.clipers.dto.TechnicalTestResponse;
import com.clipers.clipers.entity.Job;
import com.clipers.clipers.entity.TechnicalTest;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.JobRepository;
import com.clipers.clipers.repository.TechnicalTestRepository;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TechnicalTestService {
    
    private final TechnicalTestRepository technicalTestRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    
    @Value("${video.processing.service.url}")
    private String videoProcessingServiceUrl;
    
    @Autowired
    public TechnicalTestService(TechnicalTestRepository technicalTestRepository,
                               JobRepository jobRepository,
                               UserRepository userRepository,
                               NotificationService notificationService,
                               RestTemplate restTemplate) {
        this.technicalTestRepository = technicalTestRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }
    
    public TechnicalTest generateAndSendTest(String jobId, String candidateId, String companyId) {
        // Get job details
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Check if test already exists
        Optional<TechnicalTest> existingTest = technicalTestRepository
                .findByJobIdAndCandidateId(jobId, candidateId);
        
        if (existingTest.isPresent()) {
            throw new RuntimeException("Technical test already sent to this candidate");
        }
        
        // Generate test from microservice
        String testMarkdown = generateTestFromMicroservice(job);
        
        // Create and save technical test
        TechnicalTest test = new TechnicalTest(jobId, candidateId, companyId, testMarkdown);
        test = technicalTestRepository.save(test);
        
        // TODO: Send notification to candidate
        // notificationService.notifyTechnicalTestSent(candidateId, jobId, test.getId());
        
        return test;
    }
    
    private String generateTestFromMicroservice(Job job) {
        try {
            String url = videoProcessingServiceUrl + "/generate-technical-test";
            System.out.println("🔄 Calling microservice: " + url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> request = new HashMap<>();
            request.put("profession", job.getTitle());
            request.put("technologies", String.join(", ", job.getSkills()));
            request.put("experience", extractExperienceFromRequirements(job.getRequirements()));
            request.put("education", "Relevant degree or equivalent experience");
            
            System.out.println("📋 Request data:");
            System.out.println("  - profession: " + request.get("profession"));
            System.out.println("  - technologies: " + request.get("technologies"));
            System.out.println("  - experience: " + request.get("experience"));
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TechnicalTestResponse> response = restTemplate.postForEntity(
                url,
                entity,
                TechnicalTestResponse.class
            );
            
            System.out.println("✅ Microservice response status: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String markdown = response.getBody().getTechnicalTestMarkdown();
                System.out.println("📝 Received markdown length: " + (markdown != null ? markdown.length() : 0));
                return markdown;
            } else {
                System.err.println("❌ Microservice returned non-success status");
                throw new RuntimeException("Failed to generate technical test from microservice");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating technical test: " + e.getMessage());
            e.printStackTrace();
            // Return a default test if microservice fails
            System.out.println("⚠️ Using default test as fallback");
            return generateDefaultTest(job);
        }
    }
    
    private String extractExperienceFromRequirements(List<String> requirements) {
        for (String req : requirements) {
            if (req.toLowerCase().contains("año") || req.toLowerCase().contains("experiencia")) {
                return req;
            }
        }
        return "Experience in the field";
    }
    
    private String generateDefaultTest(Job job) {
        StringBuilder test = new StringBuilder();
        test.append("# Prueba Técnica - ").append(job.getTitle()).append("\n\n");
        test.append("## Información General\n");
        test.append("- **Duración estimada:** 2-3 horas\n");
        test.append("- **Posición:** ").append(job.getTitle()).append("\n");
        test.append("- **Ubicación:** ").append(job.getLocation()).append("\n\n");
        
        test.append("## Habilidades a Evaluar\n");
        for (String skill : job.getSkills()) {
            test.append("- ").append(skill).append("\n");
        }
        test.append("\n");
        
        test.append("## Requisitos del Puesto\n");
        for (String req : job.getRequirements()) {
            test.append("- ").append(req).append("\n");
        }
        test.append("\n");
        
        test.append("## Instrucciones\n");
        test.append("Esta prueba técnica está diseñada para evaluar tus habilidades y conocimientos relacionados con el puesto de **")
            .append(job.getTitle()).append("**. Por favor, lee cuidadosamente cada sección y proporciona respuestas detalladas.\n\n");
        
        test.append("## Parte 1: Preguntas Teóricas (30%)\n\n");
        test.append("### Pregunta 1: Experiencia Profesional\n");
        test.append("Describe tu experiencia trabajando con las tecnologías y habilidades mencionadas anteriormente. ");
        test.append("Incluye proyectos específicos donde hayas aplicado estas competencias.\n\n");
        
        test.append("### Pregunta 2: Resolución de Problemas\n");
        test.append("Explica un desafío técnico complejo que hayas enfrentado en tu carrera y cómo lo resolviste. ");
        test.append("¿Qué aprendiste de esa experiencia?\n\n");
        
        test.append("### Pregunta 3: Mejores Prácticas\n");
        test.append("¿Cuáles consideras que son las mejores prácticas para el desarrollo en este rol? ");
        test.append("¿Cómo aseguras la calidad del código en tus proyectos?\n\n");
        
        test.append("## Parte 2: Ejercicio Práctico (50%)\n\n");
        test.append("### Ejercicio: Desarrollo de Solución\n");
        test.append("Desarrolla una solución que demuestre tu dominio de las tecnologías requeridas para este puesto. ");
        test.append("La solución debe incluir:\n\n");
        test.append("1. **Arquitectura:** Diseño de la solución propuesta\n");
        test.append("2. **Implementación:** Código funcional que resuelva el problema\n");
        test.append("3. **Documentación:** Explicación clara de tu enfoque\n");
        test.append("4. **Testing:** Casos de prueba relevantes\n\n");
        
        test.append("**Problema a resolver:**\n");
        test.append("Diseña e implementa una funcionalidad que sea relevante para el puesto de ")
            .append(job.getTitle()).append(". ");
        test.append("Asegúrate de aplicar las mejores prácticas y demostrar tu conocimiento de las tecnologías requeridas.\n\n");
        
        test.append("## Parte 3: Caso de Estudio (20%)\n\n");
        test.append("### Escenario\n");
        test.append("Imagina que te unes a nuestro equipo y te asignan un proyecto importante. ");
        test.append("Describe cómo abordarías las siguientes situaciones:\n\n");
        test.append("1. **Planificación:** ¿Cómo organizarías tu trabajo y priorizarías tareas?\n");
        test.append("2. **Colaboración:** ¿Cómo trabajarías con otros miembros del equipo?\n");
        test.append("3. **Calidad:** ¿Qué medidas tomarías para asegurar la calidad del entregable?\n");
        test.append("4. **Innovación:** ¿Qué propuestas de mejora sugerirías?\n\n");
        
        test.append("## Criterios de Evaluación\n\n");
        test.append("Tu prueba será evaluada considerando:\n\n");
        test.append("- **Conocimiento técnico:** Dominio de las tecnologías y conceptos requeridos\n");
        test.append("- **Calidad del código:** Claridad, organización y mejores prácticas\n");
        test.append("- **Resolución de problemas:** Capacidad analítica y creatividad\n");
        test.append("- **Comunicación:** Claridad en las explicaciones y documentación\n");
        test.append("- **Atención al detalle:** Completitud y precisión en las respuestas\n\n");
        
        test.append("## Entrega\n\n");
        test.append("Por favor, completa todas las secciones de esta prueba y envía tus respuestas a través de la plataforma. ");
        test.append("Si tienes alguna pregunta, no dudes en contactarnos.\n\n");
        test.append("**¡Buena suerte!** 🚀\n");
        
        return test.toString();
    }
    
    public List<TechnicalTest> getTestsByJob(String jobId) {
        return technicalTestRepository.findByJobId(jobId);
    }
    
    public List<TechnicalTest> getTestsByCandidate(String candidateId) {
        return technicalTestRepository.findByCandidateId(candidateId);
    }
    
    public TechnicalTest submitTest(String testId, String candidateResponse) {
        TechnicalTest test = technicalTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        
        test.setCandidateResponse(candidateResponse);
        test.setStatus(TechnicalTest.TestStatus.SUBMITTED);
        test.setSubmittedAt(java.time.LocalDateTime.now());
        
        test = technicalTestRepository.save(test);
        
        // TODO: Notify company
        // notificationService.notifyTechnicalTestSubmitted(test.getCompanyId(), test.getJobId(), testId);
        
        return test;
    }
    
    public TechnicalTest reviewTest(String testId, Integer score, String feedback) {
        TechnicalTest test = technicalTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        
        test.setScore(score);
        test.setFeedback(feedback);
        test.setStatus(TechnicalTest.TestStatus.REVIEWED);
        test.setReviewedAt(java.time.LocalDateTime.now());
        
        return technicalTestRepository.save(test);
    }
    
    public List<TechnicalTest> getTestsByJobAndCandidate(String jobId, String candidateId) {
        return technicalTestRepository.findByJobIdAndCandidateId(jobId, candidateId)
                .map(List::of)
                .orElse(List.of());
    }
    
    public void deleteTest(String testId) {
        technicalTestRepository.deleteById(testId);
    }
}
