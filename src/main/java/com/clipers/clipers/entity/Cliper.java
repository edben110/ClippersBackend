package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entidad Cliper que implementa Chain of Responsibility implícitamente
 * para el procesamiento de video -> audio -> transcripción -> NLP
 */
@Document(collection = "clipers")
public class Cliper {

    @Id
    private String id;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String videoUrl;

    private String thumbnailUrl;

    @NotNull
    private Integer duration; // in seconds

    private Status status = Status.UPLOADED;

    private String transcription;

    private List<String> skills = new ArrayList<>();

    private String userId; // Referencia al usuario

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Cliper() {}

    public Cliper(String title, String description, String videoUrl, Integer duration, String userId) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.userId = userId;
    }

    // Chain of Responsibility implementado implícitamente
    public void processVideo() {
        try {
            if (canExtractAudio()) {
                extractAudio();
                if (canTranscribe()) {
                    transcribeAudio();
                    if (canAnalyzeText()) {
                        analyzeTextAndExtractSkills();
                        this.status = Status.DONE;
                    }
                }
            }
        } catch (Exception e) {
            this.status = Status.FAILED;
            System.err.println("Error procesando cliper " + this.id + ": " + e.getMessage());
        }
    }

    private boolean canExtractAudio() {
        return this.status == Status.UPLOADED && 
               this.videoUrl != null && 
               !this.videoUrl.isEmpty();
    }

    private void extractAudio() throws InterruptedException {
        // Simular extracción de audio con FFmpeg
        System.out.println("Extrayendo audio del cliper: " + this.id);
        Thread.sleep(1000); // Simular procesamiento
        this.status = Status.PROCESSING;
        System.out.println("Audio extraído exitosamente para cliper: " + this.id);
    }

    private boolean canTranscribe() {
        return this.status == Status.PROCESSING && this.transcription == null;
    }

    private void transcribeAudio() throws InterruptedException {
        // Simular transcripción con Whisper/Vosk
        System.out.println("Transcribiendo audio del cliper: " + this.id);
        Thread.sleep(2000); // Simular procesamiento
        
        // Generar transcripción simulada
        this.transcription = generateMockTranscription();
        System.out.println("Transcripción completada para cliper: " + this.id);
    }

    private boolean canAnalyzeText() {
        return this.status == Status.PROCESSING && 
               this.transcription != null && 
               !this.transcription.isEmpty() &&
               this.skills.isEmpty();
    }

    private void analyzeTextAndExtractSkills() throws InterruptedException {
        // Simular análisis NLP con Hugging Face Transformers
        System.out.println("Analizando texto y extrayendo skills del cliper: " + this.id);
        Thread.sleep(1500); // Simular procesamiento
        
        // Extraer skills usando Strategy Pattern implícito
        this.skills = extractSkillsFromTranscription(this.transcription);
        
        System.out.println("Análisis NLP completado para cliper: " + this.id + 
                         ". Skills extraídas: " + this.skills);
    }

    private String generateMockTranscription() {
        // En producción, esto vendría del servicio de transcripción
        return "Hola, soy un profesional con experiencia en desarrollo de software, especialmente en Java y Spring Boot. " +
               "Me considero una persona proactiva, con buenas habilidades de comunicación y trabajo en equipo. " +
               "Estoy buscando nuevas oportunidades para crecer profesionalmente.";
    }

    // Strategy Pattern implícito para extracción de skills
    private List<String> extractSkillsFromTranscription(String transcription) {
        List<String> detectedSkills = new ArrayList<>();
        String transcriptionLower = transcription.toLowerCase();
        
        // Aplicar diferentes estrategias de detección
        detectedSkills.addAll(extractTechnicalSkills(transcriptionLower));
        detectedSkills.addAll(extractSoftSkills(transcriptionLower));
        
        // Si no se detectaron skills específicas, agregar algunas por defecto
        if (detectedSkills.isEmpty()) {
            detectedSkills.addAll(Arrays.asList("Comunicación", "Trabajo en equipo", "Adaptabilidad"));
        }
        
        return detectedSkills;
    }

    private List<String> extractTechnicalSkills(String text) {
        List<String> technicalSkills = new ArrayList<>();
        
        if (text.contains("java")) technicalSkills.add("Java");
        if (text.contains("spring")) technicalSkills.add("Spring Boot");
        if (text.contains("python")) technicalSkills.add("Python");
        if (text.contains("javascript")) technicalSkills.add("JavaScript");
        if (text.contains("react")) technicalSkills.add("React");
        if (text.contains("angular")) technicalSkills.add("Angular");
        if (text.contains("node")) technicalSkills.add("Node.js");
        if (text.contains("base de datos") || text.contains("sql")) technicalSkills.add("SQL");
        if (text.contains("docker")) technicalSkills.add("Docker");
        if (text.contains("kubernetes")) technicalSkills.add("Kubernetes");
        
        return technicalSkills;
    }

    private List<String> extractSoftSkills(String text) {
        List<String> softSkills = new ArrayList<>();
        
        if (text.contains("comunicación")) softSkills.add("Comunicación");
        if (text.contains("trabajo en equipo") || text.contains("colaboración")) 
            softSkills.add("Trabajo en equipo");
        if (text.contains("liderazgo")) softSkills.add("Liderazgo");
        if (text.contains("proactiv")) softSkills.add("Proactividad");
        if (text.contains("resolución de problemas")) softSkills.add("Resolución de problemas");
        if (text.contains("creatividad") || text.contains("creativ")) 
            softSkills.add("Creatividad");
        
        return softSkills;
    }

    // State Pattern implícito para manejo de estados
    public boolean canBeEdited() {
        return this.status == Status.UPLOADED || this.status == Status.FAILED || this.status == Status.DONE;
    }

    public boolean isProcessingComplete() {
        return this.status == Status.DONE;
    }

    public boolean hasProcessingFailed() {
        return this.status == Status.FAILED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum Status {
        UPLOADED, PROCESSING, DONE, FAILED
    }
}