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

    @Value("${video.processing.service.url}")
    private String videoProcessingServiceUrl;

    @Value("${video.processing.service.enabled}")
    private boolean videoProcessingEnabled;

    @Value("${file.upload.base.url}")
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
     * Template Method implemented implicitly
     * Defines the flow for creating and processing Clipers
     * IMPROVEMENT: One cliper per user - automatically replaces previous one
     * IMPROVEMENT: Doesn't create simulated data if microservice fails
     */
    public Cliper createCliper(String userId, String title, String description, String videoUrl, Integer duration, org.springframework.web.multipart.MultipartFile videoFile) {
        // Step 1: Validate user
        User user = validateAndGetUser(userId);

        // Step 2: Check if user already has a cliper and delete it
        List<Cliper> existingClipers = cliperRepository.findByUserId(userId);
        if (!existingClipers.isEmpty()) {
            System.out.println("User already has " + existingClipers.size() + " cliper(s). Deleting...");
            for (Cliper existingCliper : existingClipers) {
                deleteCliperAndVideo(existingCliper);
            }
        }

        // Step 3: Validate video duration (only if real duration is provided)
        // Currently commented because we use simulated duration
        // TODO: Uncomment when we implement real duration extraction with FFmpeg
        /*
        if (duration != null && (duration < 15 || duration > 120)) {
            throw new IllegalArgumentException("Video must be between 15 seconds and 2 minutes");
        }
        */

        // Step 4: Save video file first
        String videoUrlSaved = null;
        java.nio.file.Path videoFilePath = null;
        if (videoFile != null) {
            videoUrlSaved = saveVideoFile(videoFile);
            // Use absolute path for microservice
            String fileName = videoUrlSaved.substring(videoUrlSaved.lastIndexOf('/') + 1);
            videoFilePath = java.nio.file.Paths.get("uploads/videos", fileName).toAbsolutePath();
        }

        // Step 5: Process video synchronously before saving cliper
        VideoProcessingResponse response = null;
        if (videoFilePath != null && videoProcessingEnabled) {
            response = callVideoProcessingService(videoFilePath);
        }

        // If microservice is disabled or fails, use simulated data for development
        if (response == null || response.getProfile() == null) {
            if (videoProcessingEnabled) {
                // Only fail if service is enabled but not responding
                if (videoFilePath != null) {
                    try {
                        java.nio.file.Files.deleteIfExists(videoFilePath);
                    } catch (Exception e) {
                        System.err.println("Error deleting video after failure: " + e.getMessage());
                    }
                }
                
                throw new RuntimeException(
                    "Could not process video. Processing service is not available. " +
                    "Please try again later."
                );
            } else {
                // Use simulated data for local development
                System.out.println("⚠️ Video processing service disabled - using simulated data");
                response = createSimulatedResponse(user);
            }
        }

        // Step 6: Create new cliper with processing results
        Cliper cliper = new Cliper(title, description, videoUrlSaved != null ? videoUrlSaved : videoUrl, duration, user.getId());

        // Set processing data
        cliper.setTranscription(response.getTranscription());
        cliper.setStatus(Cliper.Status.DONE);

        // Extract skills from profile if available
        if (response.getProfile() != null) {
            List<String> skills = extractSkillsFromProfile(response.getProfile());
            cliper.setSkills(skills);
        }

        // Step 7: Save cliper
        cliper = cliperRepository.save(cliper);

        // Step 8: Create/update ATS profile with microservice data
        generateOrUpdateATSProfileFromMicroservice(
            user, 
            response.getProfile(), 
            response.getTranscription(), 
            cliper.getId()
        );

        // Step 9: Send notification
        notificationService.notifyCliperProcessed(user.getId(), cliper.getId());

        return cliper;
    }

    private User validateAndGetUser(String userId) {
        // userId is the real user ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.CANDIDATE) {
            throw new IllegalArgumentException("Only candidates can create Clipers");
        }

        return user;
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
        return cliperRepository.findByStatusOrderByCreatedAtDesc(Cliper.Status.DONE, pageable);
    }

    /**
     * Enrich cliper with user information
     */
    public void enrichCliperWithUser(Cliper cliper) {
        // This is handled in the controller by creating a CliperDTO with user info
    }

    public Page<Cliper> searchClipers(String query, Pageable pageable) {
        return cliperRepository.searchClipers(query, pageable);
    }

    public List<Cliper> findBySkill(String skill) {
        return cliperRepository.findBySkillsContaining(skill);
    }

    public Cliper updateCliper(String id, String userId, String title, String description) {
        Cliper cliper = cliperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));

        // Verify ownership - only the owner can edit their cliper
        if (!cliper.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only edit your own clipers");
        }

        // State Pattern implicit - check if it can be edited
        if (!cliper.canBeEdited()) {
            throw new IllegalStateException("Cliper cannot be edited in its current state: " + cliper.getStatus());
        }

        cliper.setTitle(title);
        cliper.setDescription(description);

        return cliperRepository.save(cliper);
    }

    public void deleteCliper(String id, String userId) {
        Cliper cliper = cliperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));

        // Verify ownership - only the owner can delete their cliper
        if (!cliper.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only delete your own clipers");
        }

        // State Pattern implicit - check if it can be deleted
        if (!cliper.canBeEdited()) {
            throw new IllegalStateException("Cliper cannot be deleted in its current state: " + cliper.getStatus());
        }

        // Delete video file
        if (cliper.getVideoUrl() != null && !cliper.getVideoUrl().isEmpty()) {
            deleteVideoFile(cliper.getVideoUrl());
        }
        
        // Delete thumbnail if exists
        if (cliper.getThumbnailUrl() != null && !cliper.getThumbnailUrl().isEmpty()) {
            deleteVideoFile(cliper.getThumbnailUrl());
        }

        // Delete cliper from database
        cliperRepository.deleteById(id);
        System.out.println("✅ Cliper deleted from database: " + id);
    }

    public List<Cliper> findByStatus(Cliper.Status status) {
        return cliperRepository.findByStatus(status);
    }

    /**
     * Command Pattern implicit - retries processing of a failed Cliper
     * Note: This requires the original video file to be re-uploaded
     */
    public void retryProcessing(String cliperId) {
        Cliper cliper = cliperRepository.findById(cliperId)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));

        if (!cliper.hasProcessingFailed()) {
            throw new IllegalStateException("Can only retry processing of failed Clipers");
        }

        // For retry, we would need to re-upload the video to the microservice
        // Since we don't have the original file, mark as failed
        cliper.setStatus(Cliper.Status.FAILED);
        cliperRepository.save(cliper);
        
        throw new IllegalStateException(
            "Cannot retry processing without original video file. " +
            "Please upload the video again."
        );
    }

    /**
     * Calls external microservice to process video
     */
    private VideoProcessingResponse callVideoProcessingService(java.nio.file.Path filePath) {
        try {
            System.out.println("=== CALLING MICROSERVICE ===");
            System.out.println("URL: " + videoProcessingServiceUrl);
            System.out.println("File: " + filePath.toString());
            System.out.println("File exists: " + java.nio.file.Files.exists(filePath));
            System.out.println("File size: " + (java.nio.file.Files.exists(filePath) ? java.nio.file.Files.size(filePath) : "N/A"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.FileSystemResource(filePath.toFile()));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println("Sending request to microservice...");
            String uploadUrl = videoProcessingServiceUrl + "/upload-video";
            ResponseEntity<VideoProcessingResponse> response = restTemplate.postForEntity(
                uploadUrl,
                entity,
                VideoProcessingResponse.class
            );

            System.out.println("=== MICROSERVICE RESPONSE ===");
            System.out.println("Status: " + response.getStatusCode());
            VideoProcessingResponse responseBody = response.getBody();
            System.out.println("Body: " + responseBody);

            if (responseBody != null) {
                System.out.println("📝 Transcription: " + responseBody.getTranscription());
                if (responseBody.getProfile() != null) {
                    System.out.println("👤 Name: " + responseBody.getProfile().getName());
                    System.out.println("💼 Profession: " + responseBody.getProfile().getProfession());
                    System.out.println("📚 Experience: " + responseBody.getProfile().getExperience());
                    System.out.println("🎓 Education: " + responseBody.getProfile().getEducation());
                    System.out.println("🛠️ Technologies: " + responseBody.getProfile().getTechnologies());
                    System.out.println("🌐 Languages: " + responseBody.getProfile().getLanguages());
                    System.out.println("🏆 Achievements: " + responseBody.getProfile().getAchievements());
                    System.out.println("🤝 Soft Skills: " + responseBody.getProfile().getSoftSkills());
                } else {
                    System.out.println("⚠️ Profile is null");
                }
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Microservice responded successfully");
                return response.getBody();
            } else {
                System.err.println("❌ Microservice error: " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error calling processing microservice: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts skills from microservice profile
     */
    private List<String> extractSkillsFromProfile(VideoProcessingResponse.Profile profile) {
        List<String> skills = new java.util.ArrayList<>();

        if (profile == null) {
            return skills; // Return empty list if profile is null
        }

        // Add technologies if they exist
        if (profile.getTechnologies() != null && !profile.getTechnologies().equals("No especificado")) {
            skills.addAll(List.of(profile.getTechnologies().split(",\\s*")));
        }

        // Add profession as skill
        if (profile.getProfession() != null && !profile.getProfession().equals("No especificado")) {
            skills.add(profile.getProfession());
        }

        return skills;
    }




    /**
     * Deletes all clipers and ATS profiles (admin only)
     */
    public void clearAllData() {
        cliperRepository.deleteAll();
        atsProfileRepository.deleteAll();
    }

    /**
     * Saves video file and returns URL
     */
    private String saveVideoFile(org.springframework.web.multipart.MultipartFile videoFile) {
        try {
            // Create directory if it doesn't exist - use absolute path
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads/videos").toAbsolutePath();
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            // Sanitize filename - remove spaces and special characters
            String originalFilename = videoFile.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "video.mp4";
            }
            String sanitizedFilename = originalFilename
                .replaceAll("[^a-zA-Z0-9.-]", "_") // Replace special chars with underscore
                .replaceAll("_+", "_"); // Replace multiple underscores with single one
            
            // Generate unique filename
            String fileName = "video_" + System.currentTimeMillis() + "_" + sanitizedFilename;
            java.nio.file.Path filePath = uploadDir.resolve(fileName);

            // Save file
            java.nio.file.Files.copy(videoFile.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Video saved to: " + filePath.toAbsolutePath());

            // Return full URL for frontend access
            return fileUploadBaseUrl + "/uploads/videos/" + fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error saving video file: " + e.getMessage());
        }
    }

    /**
     * Generates or updates ATS profile using microservice data
     */
    private void generateOrUpdateATSProfileFromMicroservice(User user, VideoProcessingResponse.Profile profile, String transcription, String cliperId) {
        try {
            if (profile == null) {
                // Don't create ATS profile if there's no real data from microservice
                return;
            }
            System.out.println("🔄 GENERATING ATS PROFILE FROM MICROSERVICE");
            System.out.println("👤 User: " + user.getId());
            System.out.println("📋 Profile data:");
            System.out.println("  - Name: " + profile.getName());
            System.out.println("  - Profession: " + profile.getProfession());
            System.out.println("  - Experience: " + profile.getExperience());
            System.out.println("  - Education: " + profile.getEducation());
            System.out.println("  - Technologies: " + profile.getTechnologies());
            System.out.println("  - Soft Skills: " + profile.getSoftSkills());
            System.out.println("  - Languages: " + profile.getLanguages());
            System.out.println("  - Achievements: " + profile.getAchievements());

            Optional<ATSProfile> existingProfile = atsProfileRepository.findByUserId(user.getId());

            ATSProfile atsProfile;
            if (existingProfile.isPresent()) {
                atsProfile = existingProfile.get();
                System.out.println("📝 Updating existing profile with INTELLIGENT MERGE");
                // DON'T clear lists - do intelligent merge
            } else {
                atsProfile = new ATSProfile(user.getId());
                System.out.println("🆕 Creating new ATS profile");
            }

            // ALWAYS update summary with new cv_profile from microservice
            atsProfile.setSummary(transcription);
            System.out.println("📝 Summary updated with cv_profile from microservice");

            // INTELLIGENT MERGE: Add education only if it DOESN'T exist
            if (profile.getEducation() != null && !profile.getEducation().equals("No especificado") && !profile.getEducation().equals("Not specified")) {
                // Check if similar education already exists
                boolean educationAlreadyExists = atsProfile.getEducation().stream()
                    .anyMatch(e -> 
                        e.getInstitution().toLowerCase().contains(profile.getEducation().toLowerCase()) ||
                        e.getDegree().toLowerCase().contains(profile.getEducation().toLowerCase()) ||
                        profile.getEducation().toLowerCase().contains(e.getInstitution().toLowerCase()) ||
                        profile.getEducation().toLowerCase().contains(e.getDegree().toLowerCase())
                    );
                
                if (!educationAlreadyExists) {
                    atsProfile.addEducation(profile.getEducation(), "Degree", "Field of study");
                    System.out.println("🎓 NEW education added: " + profile.getEducation());
                } else {
                    System.out.println("🎓 Education already exists, NOT duplicating");
                }
            }

            // INTELLIGENT MERGE: Add experience only if it DOESN'T exist
            if (profile.getExperience() != null && !profile.getExperience().equals("No especificado") && !profile.getExperience().equals("Not specified")) {
                // Check if similar experience already exists
                boolean experienceAlreadyExists = atsProfile.getExperience().stream()
                    .anyMatch(e -> 
                        e.getDescription().toLowerCase().contains(profile.getExperience().toLowerCase()) ||
                        profile.getExperience().toLowerCase().contains(e.getDescription().toLowerCase())
                    );
                
                if (!experienceAlreadyExists) {
                    String position = profile.getProfession() != null && !profile.getProfession().equals("Not specified") 
                        ? profile.getProfession() 
                        : "Professional";
                    atsProfile.addExperience("Company", position, profile.getExperience());
                    System.out.println("💼 NEW experience added: " + profile.getExperience());
                } else {
                    System.out.println("💼 Experience already exists, NOT duplicating");
                }
            }

            // INTELLIGENT MERGE: Add technologies as technical skills only if they DON'T exist
            if (profile.getTechnologies() != null && !profile.getTechnologies().equals("No especificado") && !profile.getTechnologies().equals("Not specified")) {
                String[] technologies = profile.getTechnologies().split(",\\s*");
                for (String technology : technologies) {
                    String techName = technology.trim();
                    if (!techName.isEmpty() && !skillExists(atsProfile, techName)) {
                        atsProfile.addSkill(techName, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.TECHNICAL);
                        System.out.println("🛠️ NEW technical skill added: " + techName);
                    } else if (skillExists(atsProfile, techName)) {
                        System.out.println("🛠️ Technical skill already exists, NOT duplicating: " + techName);
                    }
                }
            }

            // INTELLIGENT MERGE: Add soft skills only if they DON'T exist
            if (profile.getSoftSkills() != null && !profile.getSoftSkills().equals("No especificado") && !profile.getSoftSkills().equals("Not specified")) {
                String[] softSkills = profile.getSoftSkills().split(",\\s*");
                for (String softSkill : softSkills) {
                    String skillName = softSkill.trim();
                    if (!skillName.isEmpty() && !skillExists(atsProfile, skillName)) {
                        atsProfile.addSkill(skillName, Skill.SkillLevel.INTERMEDIATE, Skill.SkillCategory.SOFT);
                        System.out.println("🤝 NEW soft skill added: " + skillName);
                    } else if (skillExists(atsProfile, skillName)) {
                        System.out.println("🤝 Soft skill already exists, NOT duplicating: " + skillName);
                    }
                }
            }

            // INTELLIGENT MERGE: Add languages only if they DON'T exist
            if (profile.getLanguages() != null && !profile.getLanguages().equals("No especificado") && !profile.getLanguages().equals("Not specified")) {
                String[] languages = profile.getLanguages().split(",\\s*");
                for (String language : languages) {
                    String languageName = language.trim();
                    if (!languageName.isEmpty() && !languageExists(atsProfile, languageName)) {
                        atsProfile.addLanguage(languageName, Language.LanguageLevel.INTERMEDIATE);
                        System.out.println("🌐 NEW language added: " + languageName);
                    } else if (languageExists(atsProfile, languageName)) {
                        System.out.println("🌐 Language already exists, NOT duplicating: " + languageName);
                    }
                }
            }

            ATSProfile savedProfile = atsProfileRepository.save(atsProfile);
            System.out.println("✅ ATS profile saved successfully with ID: " + savedProfile.getId());
            System.out.println("📊 Profile statistics:");
            System.out.println("  - Education: " + savedProfile.getEducation().size());
            System.out.println("  - Experience: " + savedProfile.getExperience().size());
            System.out.println("  - Skills: " + savedProfile.getSkills().size());
            System.out.println("  - Languages: " + savedProfile.getLanguages().size());

        } catch (Exception e) {
            System.err.println("❌ Error generating ATS profile from microservice for user " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }



    /**
     * Helper method to check if a skill already exists
     */
    private boolean skillExists(ATSProfile atsProfile, String skillName) {
        return atsProfile.getSkills().stream()
            .anyMatch(skill -> skill.getName().equalsIgnoreCase(skillName.trim()));
    }

    /**
     * Helper method to check if a language already exists (case-insensitive)
     */
    private boolean languageExists(ATSProfile atsProfile, String languageName) {
        return atsProfile.getLanguages().stream()
            .anyMatch(lang -> lang.getName().equalsIgnoreCase(languageName.trim()));
    }

    /**
     * Creates simulated response for local development
     */
    private VideoProcessingResponse createSimulatedResponse(User user) {
        VideoProcessingResponse response = new VideoProcessingResponse();
        
        // Create simulated profile
        VideoProcessingResponse.Profile profile = new VideoProcessingResponse.Profile();
        profile.setName(user.getFirstName() + " " + user.getLastName());
        profile.setProfession("Software Developer");
        profile.setExperience("3+ years of experience in web development");
        profile.setEducation("Computer Science Degree");
        profile.setTechnologies("Java, Spring Boot, React, MongoDB");
        profile.setLanguages("Spanish, English");
        profile.setAchievements("Led team of 5 developers");
        profile.setSoftSkills("Leadership, Communication, Problem Solving");
        
        response.setProfile(profile);
        response.setTranscription("This is a simulated transcription for local development. " +
            "The candidate introduces themselves and talks about their experience.");
        
        System.out.println("✅ Created simulated response for local development");
        return response;
    }

    /**
     * Deletes a cliper and its associated video
     * Implements automatic file cleanup
     */
    private void deleteCliperAndVideo(Cliper cliper) {
        try {
            System.out.println("Deleting cliper: " + cliper.getId());
            
            // Delete video
            if (cliper.getVideoUrl() != null && !cliper.getVideoUrl().isEmpty()) {
                deleteVideoFile(cliper.getVideoUrl());
            }
            
            // Delete thumbnail if exists
            if (cliper.getThumbnailUrl() != null && !cliper.getThumbnailUrl().isEmpty()) {
                deleteVideoFile(cliper.getThumbnailUrl());
            }
            
            // Delete from DB
            cliperRepository.delete(cliper);
            
            System.out.println("Cliper deleted successfully: " + cliper.getId());
            
        } catch (Exception e) {
            System.err.println("Error deleting cliper " + cliper.getId() + ": " + e.getMessage());
            // Don't throw exception to avoid interrupting new cliper creation
        }
    }

    /**
     * Deletes a video file from filesystem
     */
    private void deleteVideoFile(String videoUrl) {
        try {
            // Extract filename from URL
            String fileName = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);
            java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/videos", fileName).toAbsolutePath();
            
            // Delete file if exists
            if (java.nio.file.Files.exists(filePath)) {
                java.nio.file.Files.delete(filePath);
                System.out.println("File deleted: " + filePath);
            } else {
                System.out.println("File not found (already deleted): " + filePath);
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting video file: " + e.getMessage());
            // Don't throw exception, just log error
        }
    }

    /**
     * Toggle like on a cliper
     */
    public Cliper toggleLike(String cliperId, String userId) {
        Cliper cliper = cliperRepository.findById(cliperId)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));
        
        System.out.println("🔄 Toggle like - Cliper: " + cliperId + ", User: " + userId);
        System.out.println("📊 Before - Likes: " + cliper.getLikesCount() + ", Liked by user: " + cliper.isLikedBy(userId));
        
        if (cliper.isLikedBy(userId)) {
            cliper.removeLike(userId);
            System.out.println("❌ Removed like");
        } else {
            cliper.addLike(userId);
            System.out.println("✅ Added like");
        }
        
        System.out.println("📊 After - Likes: " + cliper.getLikesCount());
        
        Cliper saved = cliperRepository.save(cliper);
        System.out.println("💾 Saved to DB - Likes: " + saved.getLikesCount());
        
        return saved;
    }

    /**
     * Add comment to a cliper
     */
    public Cliper addComment(String cliperId, String userId, String userName, String text) {
        Cliper cliper = cliperRepository.findById(cliperId)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));
        
        System.out.println("💬 Adding comment - Cliper: " + cliperId + ", User: " + userName);
        System.out.println("📊 Before - Comments: " + cliper.getCommentsCount());
        
        Cliper.Comment comment = new Cliper.Comment(userId, userName, text);
        cliper.addComment(comment);
        
        System.out.println("📊 After - Comments: " + cliper.getCommentsCount());
        
        Cliper saved = cliperRepository.save(cliper);
        System.out.println("💾 Saved to DB - Comments: " + saved.getCommentsCount());
        
        return saved;
    }

    /**
     * Delete comment from a cliper
     */
    public Cliper deleteComment(String cliperId, String commentId, String userId) {
        Cliper cliper = cliperRepository.findById(cliperId)
                .orElseThrow(() -> new RuntimeException("Cliper not found"));
        
        // Find and remove comment if user owns it
        cliper.getComments().removeIf(comment -> 
            comment.getId().equals(commentId) && comment.getUserId().equals(userId)
        );
        
        return cliperRepository.save(cliper);
    }
}