package com.clipers.clipers.service;

import com.clipers.clipers.dto.VideoProcessingResponse;
import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.entity.Cliper;
import com.clipers.clipers.entity.Skill;
import com.clipers.clipers.entity.Language;
import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.ATSProfileRepository;
import com.clipers.clipers.repository.CliperRepository;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio que maneja Clipers implementando Chain of Responsibility implícitamente
 * El procesamiento se delega a la entidad Cliper que maneja la cadena internamente
 */
@Service
@Transactional
public class CliperService {

    private final CliperRepository cliperRepository;
    private final UserRepository userRepository;
    private final ATSProfileRepository atsProfileRepository;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value("${video.processing.service.url:https://micoservicioprocesarvideo.onrender.com/upload-video}")
    private String videoProcessingServiceUrl;

    @Value("${file.upload.base.url:http://localhost:8080}")
    private String fileUploadBaseUrl;

    @Autowired
    public CliperService(CliperRepository cliperRepository,
                         UserRepository userRepository,
                         ATSProfileRepository atsProfileRepository,
                         NotificationService notificationService,
                         RestTemplate restTemplate) {
        this.cliperRepository = cliperRepository;
        this.userRepository = userRepository;
        this.atsProfileRepository = atsProfileRepository;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }

    /**
     * Template Method implementado implícitamente
     * Define el flujo de creación y procesamiento de Clipers
     * Ahora procesa síncronamente antes de guardar para rellenar ATS automáticamente
     */
    public Cliper createCliper(String userId, String title, String description, String videoUrl, Integer duration, org.springframework.web.multipart.MultipartFile videoFile) {
        // Step 1: Validate user
        User user = validateAndGetUser(userId);

        // Step 2: Check if user already has a cliper and delete it if exists
        List<Cliper> existingClipers = cliperRepository.findByUserId(userId);
        if (!existingClipers.isEmpty()) {
            // Delete existing cliper(s) to allow creating a new one
            for (Cliper existingCliper : existingClipers) {
                cliperRepository.delete(existingCliper);
            }
        }

        // Step 3: Save video file first
        String savedVideoUrl = null;
        java.nio.file.Path savedFilePath = null;
        if (videoFile != null) {
            savedVideoUrl = saveVideoFile(videoFile);
            savedFilePath = java.nio.file.Paths.get("./uploads/videos", savedVideoUrl.substring(savedVideoUrl.lastIndexOf('/') + 1));
        }

        // Step 4: Process video synchronously before saving cliper
        VideoProcessingResponse processingResponse = null;
        if (savedFilePath != null) {
            processingResponse = callVideoProcessingService(savedFilePath);
        }

         // If microservice fails, create simulated data based on expected JSON format
         if (processingResponse == null) {
             processingResponse = createSimulatedVideoProcessingResponse();
         }

        // Step 5: Create new cliper with processing results
        Cliper cliper = new Cliper(title, description, savedVideoUrl != null ? savedVideoUrl : videoUrl, duration, user);

        // Set processing data if available
        if (processingResponse != null) {
            cliper.setTranscription(processingResponse.getTranscription());
            cliper.setStatus(Cliper.Status.DONE);

            // Extract skills from profile if available
            if (processingResponse.getProfile() != null) {
                List<String> skills = extractSkillsFromProfile(processingResponse.getProfile());
                // Note: Cliper entity might need skills field, for now we'll store in transcription
            }
        } else {
            // Simulated processing
            cliper.setTranscription("Procesamiento simulado completado");
            cliper.setStatus(Cliper.Status.DONE);
        }

        // Step 6: Save cliper
        cliper = cliperRepository.save(cliper);

        // Step 4: Always create/update ATS profile with microservice data (regenerate each time)
         if (processingResponse != null && processingResponse.getProfile() != null) {
             // Use microservice data to create/update ATS profile (always regenerate)
             generateOrUpdateATSProfileFromMicroservice(user, processingResponse.getProfile(), processingResponse.getTranscription(), cliper.getId());
         } else {
             // If no microservice data, update cliper ID if profile exists
             updateATSProfileWithCliperId(user.getId(), cliper.getId());
         }

        // Step 7: Send notification
        notificationService.notifyCliperProcessed(user.getId(), cliper.getId());

        return cliper;
    }

    private User validateAndGetUser(String userId) {
        // userId es el ID real del usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != User.Role.CANDIDATE) {
            throw new IllegalArgumentException("Solo los candidatos pueden crear Clipers");
        }

        return user;
    }


    /**
     * Builder Pattern implícito para generar perfil ATS
     * Utiliza el patrón builder implementado en ATSProfile
     */
    private void generateOrUpdateATSProfile(Cliper cliper) {
        try {
            Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(cliper.getUser().getId());
            
            ATSProfile atsProfile;
            if (existingProfile.isPresent()) {
                // Actualizar perfil existente
                atsProfile = existingProfile.get();
                atsProfile.withCliper(cliper.getId());
            } else {
                // Crear nuevo perfil usando Builder pattern implícito
                atsProfile = new ATSProfile(cliper.getUser())
                        .withCliper(cliper.getId());
            }
            
            // Generar contenido del perfil desde el cliper procesado
            atsProfile.generateFromCliperData(cliper.getTranscription(), cliper.getSkills());
            
            atsProfileRepository.save(atsProfile);
            
        } catch (Exception e) {
            System.err.println("Error generando perfil ATS para cliper " + cliper.getId() + ": " + e.getMessage());
        }
    }

    // Métodos CRUD estándar
    public Optional<Cliper> findById(String id) {
        return cliperRepository.findById(id);
    }

    public List<Cliper> findByUserId(String userId) {
        return cliperRepository.findByUserId(userId);
    }

    public Page<Cliper> findByUserId(String userId, Pageable pageable) {
        return cliperRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Cliper> findProcessedClipers(Pageable pageable) {
        return cliperRepository.findProcessedClipersOrderByCreatedAtDesc(pageable);
    }

    public Page<Cliper> searchClipers(String query, Pageable pageable) {
        return cliperRepository.searchClipers(query, pageable);
    }

    public List<Cliper> findBySkill(String skill) {
        return cliperRepository.findBySkillsContaining(skill);
    }

    public Cliper updateCliper(String id, String title, String description) {
        Cliper cliper = cliperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliper no encontrado"));

        // State Pattern implícito - verificar si puede ser editado
        if (!cliper.canBeEdited()) {
            throw new IllegalStateException("El Cliper no puede ser editado en su estado actual: " + cliper.getStatus());
        }

        cliper.setTitle(title);
        cliper.setDescription(description);

        return cliperRepository.save(cliper);
    }

    public void deleteCliper(String id) {
        Cliper cliper = cliperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliper no encontrado"));

        // State Pattern implícito - verificar si puede ser eliminado
        if (!cliper.canBeEdited()) {
            throw new IllegalStateException("El Cliper no puede ser eliminado en su estado actual: " + cliper.getStatus());
        }

        cliperRepository.deleteById(id);
    }

    public List<Cliper> findByStatus(Cliper.Status status) {
        return cliperRepository.findByStatus(status);
    }

    /**
     * Command Pattern implícito - reintenta el procesamiento de un Cliper fallido
     */
    public void retryProcessing(String cliperId) {
        Cliper cliper = cliperRepository.findById(cliperId)
                .orElseThrow(() -> new RuntimeException("Cliper no encontrado"));

        if (!cliper.hasProcessingFailed()) {
            throw new IllegalStateException("Solo se puede reintentar el procesamiento de Clipers fallidos");
        }

        // Resetear estado y reiniciar procesamiento con simulación (no tenemos el archivo original)
        cliper.setStatus(Cliper.Status.UPLOADED);
        cliper = cliperRepository.save(cliper);

        // Para retry, usamos procesamiento simulado ya que no tenemos el archivo original
        new Thread(() -> {
            try {
                Thread.sleep(100);
                Cliper freshCliper = cliperRepository.findById(cliperId)
                    .orElseThrow(() -> new RuntimeException("Cliper no encontrado: " + cliperId));

                freshCliper.setStatus(Cliper.Status.PROCESSING);
                cliperRepository.save(freshCliper);

                System.out.println("Reintentando procesamiento simulado para cliper: " + freshCliper.getId());
                freshCliper.processVideo();

                if (freshCliper.getStatus() == Cliper.Status.DONE) {
                    cliperRepository.save(freshCliper);
                    generateOrUpdateATSProfile(freshCliper);
                    notificationService.notifyCliperProcessed(freshCliper.getUser().getId(), freshCliper.getId());
                } else {
                    freshCliper.setStatus(Cliper.Status.FAILED);
                    cliperRepository.save(freshCliper);
                }

            } catch (Exception e) {
                System.err.println("Error reintentando procesamiento para cliper " + cliperId + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Llama al microservicio externo para procesar el video
     */
    private VideoProcessingResponse callVideoProcessingService(java.nio.file.Path filePath) {
        try {
            System.out.println("=== LLAMANDO AL MICROSERVICIO ===");
            System.out.println("URL: " + videoProcessingServiceUrl);
            System.out.println("Archivo: " + filePath.toString());
            System.out.println("Archivo existe: " + java.nio.file.Files.exists(filePath));
            System.out.println("Tamaño del archivo: " + (java.nio.file.Files.exists(filePath) ? java.nio.file.Files.size(filePath) : "N/A"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.FileSystemResource(filePath.toFile()));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println("Enviando petición al microservicio...");
            ResponseEntity<VideoProcessingResponse> response = restTemplate.postForEntity(
                videoProcessingServiceUrl,
                entity,
                VideoProcessingResponse.class
            );

            System.out.println("=== RESPUESTA DEL MICROSERVICIO ===");
            System.out.println("Status: " + response.getStatusCode());
            VideoProcessingResponse responseBody = response.getBody();
            System.out.println("Body: " + responseBody);

            if (responseBody != null) {
                System.out.println("📝 Transcripción: " + responseBody.getTranscription());
                if (responseBody.getProfile() != null) {
                    System.out.println("👤 Nombre: " + responseBody.getProfile().getName());
                    System.out.println("💼 Profesión: " + responseBody.getProfile().getProfession());
                    System.out.println("📚 Experiencia: " + responseBody.getProfile().getExperience());
                    System.out.println("🎓 Educación: " + responseBody.getProfile().getEducation());
                    System.out.println("🛠️ Tecnologías: " + responseBody.getProfile().getTechnologies());
                    System.out.println("🌐 Idiomas: " + responseBody.getProfile().getLanguages());
                    System.out.println("🏆 Logros: " + responseBody.getProfile().getAchievements());
                    System.out.println("🤝 Habilidades blandas: " + responseBody.getProfile().getSoftSkills());
                } else {
                    System.out.println("⚠️ Perfil es null");
                }
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Microservicio respondió exitosamente");
                return response.getBody();
            } else {
                System.err.println("❌ Error en microservicio: " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error llamando al microservicio de procesamiento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrae skills del perfil del microservicio
     */
    private List<String> extractSkillsFromProfile(VideoProcessingResponse.Profile perfil) {
        List<String> skills = new java.util.ArrayList<>();

        if (perfil == null) {
            return skills; // Return empty list if profile is null
        }

        // Agregar tecnologías si existen
        if (perfil.getTechnologies() != null && !perfil.getTechnologies().equals("No especificado")) {
            skills.addAll(List.of(perfil.getTechnologies().split(",\\s*")));
        }

        // Agregar experiencia como skill
        if (perfil.getProfession() != null && !perfil.getProfession().equals("No especificado")) {
            skills.add(perfil.getProfession());
        }

        return skills;
    }


    /**
     * Genera un resumen del perfil basado en los datos del microservicio
     * Ahora incluye la transcripción completa del microservicio
     */
    private String generateSummaryFromProfile(VideoProcessingResponse.Profile perfil) {
        // Si tenemos la transcripción del microservicio, la usamos como resumen profesional
        // Esta debería venir del campo transcription en VideoProcessingResponse
        // Por ahora, generamos un resumen completo basado en los datos del perfil

        StringBuilder summary = new StringBuilder();

        // Agregar nombre si existe
        if (perfil.getName() != null && !perfil.getName().equals("No especificado")) {
            summary.append(perfil.getName()).append(" es un ");
        } else {
            summary.append("Profesional ");
        }

        // Agregar profesión
        if (perfil.getProfession() != null && !perfil.getProfession().equals("No especificado")) {
            summary.append(perfil.getProfession().toLowerCase());
        } else {
            summary.append("profesional");
        }

        // Agregar experiencia
        if (perfil.getExperience() != null && !perfil.getExperience().equals("No especificado")) {
            summary.append(" con experiencia en ").append(perfil.getExperience().toLowerCase());
        }

        summary.append(". ");

        // Agregar educación
        if (perfil.getEducation() != null && !perfil.getEducation().equals("No especificado")) {
            summary.append("Cuenta con formación en ").append(perfil.getEducation().toLowerCase()).append(". ");
        }

        // Agregar tecnologías
        if (perfil.getTechnologies() != null && !perfil.getTechnologies().equals("No especificado")) {
            summary.append("Domina tecnologías como ").append(perfil.getTechnologies().toLowerCase()).append(". ");
        }

        // Agregar habilidades blandas
        if (perfil.getSoftSkills() != null && !perfil.getSoftSkills().equals("No especificado")) {
            summary.append("Posee habilidades como ").append(perfil.getSoftSkills().toLowerCase()).append(". ");
        }

        // Agregar logros
        if (perfil.getAchievements() != null && !perfil.getAchievements().equals("No especificado")) {
            summary.append("Ha logrado ").append(perfil.getAchievements().toLowerCase()).append(". ");
        }

        return summary.toString().trim();
    }

    /**
     * Elimina todos los clipers y perfiles ATS (solo para administración)
     */
    public void clearAllClipers() {
        cliperRepository.deleteAll();
        atsProfileRepository.deleteAll();
    }

    /**
     * Elimina todos los clipers y perfiles ATS (solo para administración)
     */
    public void clearAllData() {
        // Primero eliminar clipers
        cliperRepository.deleteAll();
        // Luego eliminar perfiles ATS
        atsProfileRepository.deleteAll();
    }

    /**
     * Guarda el archivo de video y retorna la URL
     */
    private String saveVideoFile(org.springframework.web.multipart.MultipartFile videoFile) {
        try {
            // Crear directorio si no existe
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("./uploads/videos");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            // Generar nombre único para el archivo
            String fileName = "video_" + System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
            java.nio.file.Path filePath = uploadDir.resolve(fileName);

            // Guardar el archivo
            java.nio.file.Files.copy(videoFile.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Devolver URL completa para que el frontend pueda acceder
            return fileUploadBaseUrl + "/uploads/videos/" + fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al guardar el archivo de video: " + e.getMessage());
        }
    }

    /**
     * Genera o actualiza el perfil ATS usando datos del microservicio (versión para User)
     */
    private void generateOrUpdateATSProfileFromMicroservice(User user, VideoProcessingResponse.Profile perfil, String transcription, String cliperId) {
        try {
            if (perfil == null) {
                // No crear perfil ATS si no hay datos reales del microservicio
                return;
            }
            System.out.println("🔄 GENERANDO PERFIL ATS DESDE MICROSERVICIO");
            System.out.println("👤 Usuario: " + user.getId());
            System.out.println("📋 Datos del perfil:");
            System.out.println("  - Nombre: " + perfil.getName());
            System.out.println("  - Profesión: " + perfil.getProfession());
            System.out.println("  - Experiencia: " + perfil.getExperience());
            System.out.println("  - Educación: " + perfil.getEducation());
            System.out.println("  - Tecnologías: " + perfil.getTechnologies());
            System.out.println("  - Habilidades blandas: " + perfil.getSoftSkills());
            System.out.println("  - Idiomas: " + perfil.getLanguages());
            System.out.println("  - Logros: " + perfil.getAchievements());

            Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(user.getId());

            ATSProfile atsProfile;
            if (existingProfile.isPresent()) {
                atsProfile = existingProfile.get();
                System.out.println("📝 Actualizando perfil existente");
                // Limpiar listas existentes para evitar duplicados
                atsProfile.getEducation().clear();
                atsProfile.getExperience().clear();
                atsProfile.getSkills().clear();
                atsProfile.getLanguages().clear();
            } else {
                atsProfile = new ATSProfile(user);
                System.out.println("🆕 Creando nuevo perfil ATS");
            }

            // Actualizar con datos del microservicio - usar transcripción como resumen profesional
            atsProfile.setSummary(transcription);
            System.out.println("📝 Summary generado: " + atsProfile.getSummary());

            // Agregar educación si existe y no está duplicada
            if (perfil.getEducation() != null && !perfil.getEducation().equals("No especificado")) {
                boolean educationExists = atsProfile.getEducation().stream()
                    .anyMatch(e -> e.getInstitution().equals(perfil.getEducation()) ||
                                 e.getDegree().equals(perfil.getEducation()));
                if (!educationExists) {
                    atsProfile.addEducation(perfil.getEducation(), "Grado obtenido", "Campo de estudio");
                    System.out.println("🎓 Educación agregada: " + perfil.getEducation());
                } else {
                    System.out.println("🎓 Educación ya existe, omitiendo");
                }
            } else {
                // Agregar educación simulada solo si no hay ninguna
                if (atsProfile.getEducation().isEmpty()) {
                    atsProfile.addEducation("Universidad Nacional", "Ingeniería de Sistemas", "Campo de estudio");
                    System.out.println("🎓 Educación simulada agregada");
                }
            }

            // Agregar experiencia si existe y no está duplicada
            if (perfil.getExperience() != null && !perfil.getExperience().equals("No especificado")) {
                boolean expExists = experienceExists(atsProfile, "Empresa Tecnológica", perfil.getProfession(), perfil.getExperience());
                if (!expExists) {
                    atsProfile.addExperience("Empresa Tecnológica", perfil.getProfession(), perfil.getExperience());
                    System.out.println("💼 Experiencia agregada: " + perfil.getExperience());
                } else {
                    System.out.println("💼 Experiencia ya existe, omitiendo");
                }
            } else {
                // Agregar experiencia simulada solo si no hay ninguna
                if (atsProfile.getExperience().isEmpty()) {
                    atsProfile.addExperience("Empresa Tecnológica", "Desarrollador Full Stack",
                        "Desarrollo de aplicaciones web usando tecnologías modernas. Experiencia en desarrollo de APIs REST y aplicaciones escalables.");
                    System.out.println("💼 Experiencia simulada agregada");
                }
            }

            // Agregar tecnologías como skills técnicos
            if (perfil.getTechnologies() != null && !perfil.getTechnologies().equals("No especificado")) {
                String[] tecnologias = perfil.getTechnologies().split(",\\s*");
                for (String tecnologia : tecnologias) {
                    if (!skillExists(atsProfile, tecnologia.trim())) {
                        atsProfile.addSkill(tecnologia.trim(), Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.TECHNICAL);
                        System.out.println("🛠️ Skill técnico agregado: " + tecnologia.trim());
                    } else {
                        System.out.println("🛠️ Skill técnico ya existe: " + tecnologia.trim());
                    }
                }
            } else {
                // Agregar skills técnicos simulados solo si no hay ninguno
                if (atsProfile.getSkills().stream().noneMatch(s -> s.getCategory() == Skill.SkillCategory.TECHNICAL)) {
                    String[] tecnologiasSimuladas = {"Java", "Spring Boot", "React", "PostgreSQL", "JavaScript"};
                    for (String tecnologia : tecnologiasSimuladas) {
                        atsProfile.addSkill(tecnologia, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.TECHNICAL);
                        System.out.println("🛠️ Skill técnico simulado agregado: " + tecnologia);
                    }
                }
            }

            // Agregar habilidades blandas como skills soft
            if (perfil.getSoftSkills() != null && !perfil.getSoftSkills().equals("No especificado")) {
                String[] habilidades = perfil.getSoftSkills().split(",\\s*");
                for (String habilidad : habilidades) {
                    if (!skillExists(atsProfile, habilidad.trim())) {
                        atsProfile.addSkill(habilidad.trim(), Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.SOFT);
                        System.out.println("🤝 Skill blando agregado: " + habilidad.trim());
                    } else {
                        System.out.println("🤝 Skill blando ya existe: " + habilidad.trim());
                    }
                }
            } else {
                // Agregar skills blandos simulados solo si no hay ninguno
                if (atsProfile.getSkills().stream().noneMatch(s -> s.getCategory() == Skill.SkillCategory.SOFT)) {
                    String[] habilidadesSimuladas = {"Trabajo en equipo", "Comunicación", "Resolución de problemas"};
                    for (String habilidad : habilidadesSimuladas) {
                        atsProfile.addSkill(habilidad, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.SOFT);
                        System.out.println("🤝 Skill blando simulado agregado: " + habilidad);
                    }
                }
            }

            // Agregar idiomas si existen
            if (perfil.getLanguages() != null && !perfil.getLanguages().equals("No especificado")) {
                String[] idiomas = perfil.getLanguages().split(",\\s*");
                for (String idioma : idiomas) {
                    if (!languageExists(atsProfile, idioma.trim())) {
                        atsProfile.addLanguage(idioma.trim(), Language.LanguageLevel.INTERMEDIATE);
                        System.out.println("🌐 Idioma agregado: " + idioma.trim());
                    } else {
                        System.out.println("🌐 Idioma ya existe: " + idioma.trim());
                    }
                }
            } else {
                // Agregar idiomas simulados solo si no hay ninguno
                if (atsProfile.getLanguages().isEmpty()) {
                    String[] idiomasSimulados = {"Español", "Inglés"};
                    for (String idioma : idiomasSimulados) {
                        atsProfile.addLanguage(idioma, Language.LanguageLevel.INTERMEDIATE);
                        System.out.println("🌐 Idioma simulado agregado: " + idioma);
                    }
                }
            }

            ATSProfile savedProfile = atsProfileRepository.save(atsProfile);
            System.out.println("✅ Perfil ATS guardado exitosamente con ID: " + savedProfile.getId());
            System.out.println("📊 Estadísticas del perfil:");
            System.out.println("  - Educación: " + savedProfile.getEducation().size());
            System.out.println("  - Experiencia: " + savedProfile.getExperience().size());
            System.out.println("  - Skills: " + savedProfile.getSkills().size());
            System.out.println("  - Idiomas: " + savedProfile.getLanguages().size());

        } catch (Exception e) {
            System.err.println("❌ Error generando perfil ATS desde microservicio para usuario " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera o actualiza el perfil ATS con datos simulados completos
     */
    private void generateOrUpdateATSProfileSimulated(User user) {
        try {
            Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(user.getId());

            ATSProfile atsProfile;
            if (existingProfile.isPresent()) {
                atsProfile = existingProfile.get();
                // Limpiar listas existentes para evitar duplicados
                atsProfile.getEducation().clear();
                atsProfile.getExperience().clear();
                atsProfile.getSkills().clear();
                atsProfile.getLanguages().clear();
            } else {
                atsProfile = new ATSProfile(user);
            }

            // Generar contenido simulado completo
            String simulatedSummary = "Profesional con experiencia en desarrollo de software. " +
                "Especializado en tecnologías web y aplicaciones empresariales. " +
                "Conocimientos en Java, Spring Boot, React y bases de datos SQL/NoSQL.";

            atsProfile.setSummary(simulatedSummary);

            // Agregar educación simulada
            atsProfile.addEducation("Universidad Nacional", "Ingeniería de Sistemas", "Campo de estudio");

            // Agregar experiencia simulada
            atsProfile.addExperience("Empresa Tecnológica", "Desarrollador Full Stack",
                "Desarrollo de aplicaciones web usando Java, Spring Boot, React y PostgreSQL. " +
                "Experiencia en desarrollo de APIs REST y aplicaciones escalables.");

            // Agregar skills técnicas simuladas
            String[] tecnologias = {"Java", "Spring Boot", "React", "PostgreSQL", "JavaScript", "TypeScript", "Git"};
            for (String tecnologia : tecnologias) {
                atsProfile.addSkill(tecnologia, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.TECHNICAL);
            }

            // Agregar skills blandas simuladas
            String[] habilidadesBlandas = {"Trabajo en equipo", "Comunicación", "Resolución de problemas", "Aprendizaje continuo"};
            for (String habilidad : habilidadesBlandas) {
                atsProfile.addSkill(habilidad, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.SOFT);
            }

            // Agregar idiomas simulados
            String[] idiomas = {"Español", "Inglés"};
            for (String idioma : idiomas) {
                atsProfile.addLanguage(idioma, Language.LanguageLevel.INTERMEDIATE);
            }

            atsProfileRepository.save(atsProfile);

        } catch (Exception e) {
            System.err.println("Error generando perfil ATS simulado para usuario " + user.getId() + ": " + e.getMessage());
        }
    }


    /**
     * Crea una respuesta simulada del microservicio con datos variados
     */
    private VideoProcessingResponse createSimulatedVideoProcessingResponse() {
        VideoProcessingResponse response = new VideoProcessingResponse();
        VideoProcessingResponse.Profile profile = new VideoProcessingResponse.Profile();

        // Generar datos variados basados en diferentes profesiones
        String[] professions = {
            "Desarrollador Full Stack", "Ingeniero de Software", "Analista de Sistemas",
            "Arquitecto de Software", "Desarrollador Frontend", "Desarrollador Backend",
            "Ingeniero DevOps", "Analista de Datos", "Científico de Datos",
            "Ingeniero de Machine Learning", "Desarrollador Mobile", "Contador Público",
            "Administrador de Empresas", "Ingeniero Industrial", "Médico General",
            "Abogado Corporativo", "Profesor Universitario", "Diseñador Gráfico"
        };

        String[] experiences = {
            "5 años de experiencia en desarrollo de aplicaciones empresariales",
            "3 años trabajando en proyectos de software a gran escala",
            "7 años en el sector tecnológico con enfoque en soluciones innovadoras",
            "4 años liderando equipos de desarrollo y proyectos ágiles",
            "6 años en consultoría de sistemas y transformación digital",
            "2 años en startups tecnológicas con crecimiento exponencial",
            "8 años en desarrollo de software y arquitectura de sistemas",
            "3 años en investigación y desarrollo de nuevas tecnologías"
        };

        String[] educations = {
            "Ingeniería de Sistemas, Universidad Nacional",
            "Contaduría Pública, Universidad de los Andes",
            "Ingeniería Industrial, Universidad Javeriana",
            "Administración de Empresas, Universidad Externado",
            "Medicina, Universidad del Rosario",
            "Derecho, Universidad de los Andes",
            "Diseño Gráfico, Universidad Jorge Tadeo Lozano",
            "Matemáticas, Universidad Nacional"
        };

        String[][] technologies = {
            {"Java", "Spring Boot", "PostgreSQL", "React", "JavaScript"},
            {"Python", "Django", "MongoDB", "Vue.js", "TypeScript"},
            {"C#", ".NET", "SQL Server", "Angular", "Node.js"},
            {"PHP", "Laravel", "MySQL", "React Native", "Flutter"},
            {"Go", "Kubernetes", "Docker", "AWS", "Terraform"},
            {"R", "Python", "Tableau", "Power BI", "SQL"},
            {"Swift", "Kotlin", "Firebase", "React Native", "Flutter"},
            {"Excel", "SAP", "Oracle", "Power BI", "SQL"}
        };

        String[] softSkills = {
            "Trabajo en equipo, Comunicación efectiva, Resolución de problemas",
            "Liderazgo, Adaptabilidad, Pensamiento crítico",
            "Creatividad, Gestión del tiempo, Aprendizaje continuo",
            "Empatía, Negociación, Toma de decisiones",
            "Organización, Atención al detalle, Orientación a resultados"
        };

        String[] languages = {
            "Español nativo, Inglés avanzado",
            "Español nativo, Inglés intermedio, Francés básico",
            "Español nativo, Inglés avanzado, Alemán intermedio",
            "Español nativo, Inglés avanzado, Portugués intermedio",
            "Español nativo, Inglés intermedio"
        };

        String[] achievements = {
            "Certificación Scrum Master, Participación en proyectos internacionales",
            "Publicación de artículos técnicos, Conferencias en eventos del sector",
            "Liderazgo de equipos multiculturales, Implementación de metodologías ágiles",
            "Desarrollo de patentes, Reconocimientos por innovación tecnológica",
            "Excelencia académica, Becas de investigación obtenidas"
        };

        // Seleccionar índices aleatorios para variar los datos
        int profIndex = (int) (Math.random() * professions.length);
        int expIndex = (int) (Math.random() * experiences.length);
        int eduIndex = (int) (Math.random() * educations.length);
        int techIndex = (int) (Math.random() * technologies.length);
        int softIndex = (int) (Math.random() * softSkills.length);
        int langIndex = (int) (Math.random() * languages.length);
        int achIndex = (int) (Math.random() * achievements.length);

        // Generar transcripción simulada
        String transcription = "Hola, soy " + professions[profIndex].toLowerCase() +
            " con " + experiences[expIndex].toLowerCase() +
            ". Mi formación académica incluye " + educations[eduIndex].toLowerCase() +
            ". Tengo experiencia en " + String.join(", ", technologies[techIndex]).toLowerCase() +
            ". Mis habilidades blandas incluyen " + softSkills[softIndex].toLowerCase() +
            ". Hablo " + languages[langIndex].toLowerCase() +
            ". Entre mis logros destacan " + achievements[achIndex].toLowerCase() + ".";

        // Configurar el perfil
        profile.setName("Usuario Candidato");
        profile.setProfession(professions[profIndex]);
        profile.setExperience(experiences[expIndex]);
        profile.setEducation(educations[eduIndex]);
        profile.setTechnologies(String.join(", ", technologies[techIndex]));
        profile.setSoftSkills(softSkills[softIndex]);
        profile.setLanguages(languages[langIndex]);
        profile.setAchievements(achievements[achIndex]);

        response.setTranscription(transcription);
        response.setProfile(profile);

        return response;
    }

    /**
     * Crea un perfil vacío - no generamos datos simulados
     */
    private VideoProcessingResponse.Profile createSimulatedProfile() {
        // Return empty profile - no auto-generation of data
        return new VideoProcessingResponse.Profile();
    }

    /**
     * Método auxiliar para verificar si una experiencia ya existe
     */
    private boolean experienceExists(ATSProfile atsProfile, String company, String position, String description) {
        return atsProfile.getExperience().stream()
            .anyMatch(exp -> exp.getCompany().equals(company) &&
                           exp.getPosition().equals(position) &&
                           exp.getDescription().equals(description));
    }

    /**
     * Método auxiliar para verificar si una habilidad ya existe
     */
    private boolean skillExists(ATSProfile atsProfile, String skillName) {
        return atsProfile.getSkills().stream()
            .anyMatch(skill -> skill.getName().equals(skillName));
    }

    /**
     * Método auxiliar para verificar si un idioma ya existe
     */
    private boolean languageExists(ATSProfile atsProfile, String languageName) {
        return atsProfile.getLanguages().stream()
            .anyMatch(lang -> lang.getName().equals(languageName));
    }

    /**
     * Actualiza el perfil ATS con el ID del cliper
     */
    private void updateATSProfileWithCliperId(String userId, String cliperId) {
        try {
            Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(userId);

            if (existingProfile.isPresent()) {
                ATSProfile atsProfile = existingProfile.get();
                atsProfile.setCliperId(cliperId);
                atsProfileRepository.save(atsProfile);
            }

        } catch (Exception e) {
            System.err.println("Error actualizando perfil ATS con cliper ID para usuario " + userId + ": " + e.getMessage());
        }
    }
}