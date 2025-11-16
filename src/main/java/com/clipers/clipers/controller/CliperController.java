package com.clipers.clipers.controller;

import com.clipers.clipers.entity.Cliper;
import com.clipers.clipers.service.CliperService;
import com.clipers.clipers.dto.CliperDTO;
import com.clipers.clipers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/clipers")
@CrossOrigin(origins = "*")
public class CliperController {

    private final CliperService cliperService;
    private final UserRepository userRepository;

    @Value("${file.upload.base.url}")
    private String fileUploadBaseUrl;

    @Autowired
    public CliperController(CliperService cliperService, UserRepository userRepository) {
        this.cliperService = cliperService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CliperDTO> uploadCliper(
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {
        try {
            String userId = getCurrentUserId();

            // Simular guardado de archivo y obtener URL
            String videoUrl = saveVideoFile(videoFile);
            Integer duration = extractVideoDuration(videoFile);

            Cliper cliper = cliperService.createCliper(userId, title, description, videoUrl, duration, videoFile);
            return ResponseEntity.ok(new CliperDTO(cliper));
        } catch (Exception e) {
            throw new RuntimeException("Error al subir cliper: " + e.getMessage(), e);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CliperDTO> createCliper(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();

            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String videoUrl = (String) request.get("videoUrl");
            Integer duration = (Integer) request.get("duration");

            // Para este endpoint sin archivo, pasamos null y usaremos procesamiento simulado
            Cliper cliper = cliperService.createCliper(userId, title, description, videoUrl, duration, null);
            return ResponseEntity.ok(new CliperDTO(cliper));
        } catch (Exception e) {
            throw new RuntimeException("Error al crear cliper: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CliperDTO> getCliper(@PathVariable String id) {
        return cliperService.findById(id)
                .map(cliper -> ResponseEntity.ok(new CliperDTO(cliper)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CliperDTO>> getClipersByUser(@PathVariable String userId) {
        List<Cliper> clipers = cliperService.findByUserId(userId);
        List<CliperDTO> cliperDTOs = clipers.stream()
                .map(CliperDTO::new)
                .toList();
        return ResponseEntity.ok(cliperDTOs);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CliperDTO>> getMyClipers() {
        try {
            String userId = getCurrentUserId();
            List<Cliper> clipers = cliperService.findByUserId(userId);
            List<CliperDTO> cliperDTOs = clipers.stream()
                    .map(CliperDTO::new)
                    .toList();
            return ResponseEntity.ok(cliperDTOs);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener mis clipers: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/admin/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> clearAllClipers() {
        try {
            cliperService.clearAllData();
            return ResponseEntity.ok("Todos los clipers y perfiles ATS han sido eliminados");
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar datos: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/admin/clear-all-data")
    public ResponseEntity<String> clearAllData() {
        try {
            cliperService.clearAllData();
            return ResponseEntity.ok("Todos los clipers y perfiles ATS han sido eliminados");
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar datos: " + e.getMessage(), e);
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<CliperDTO>> getClipersByUserPaginated(
            @PathVariable String userId, Pageable pageable) {
        Page<Cliper> clipers = cliperService.findByUserId(userId, pageable);
        Page<CliperDTO> cliperDTOs = clipers.map(CliperDTO::new);
        return ResponseEntity.ok(cliperDTOs);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getClipers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Cliper> clipersPage = cliperService.findProcessedClipers(pageable);

        List<CliperDTO> cliperDTOs = clipersPage.getContent().stream()
                .map(CliperDTO::new)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("clipers", cliperDTOs);
        response.put("hasMore", clipersPage.hasNext());
        response.put("totalPages", clipersPage.getTotalPages());
        response.put("currentPage", page);
        response.put("totalElements", clipersPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getProcessedClipers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Cliper> clipersPage = cliperService.findProcessedClipers(pageable);

        List<CliperDTO> cliperDTOs = clipersPage.getContent().stream()
                .map(CliperDTO::new)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("clipers", cliperDTOs);
        response.put("hasMore", clipersPage.hasNext());
        response.put("totalPages", clipersPage.getTotalPages());
        response.put("currentPage", page);
        response.put("totalElements", clipersPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CliperDTO>> searchClipers(
            @RequestParam String query, Pageable pageable) {
        Page<Cliper> clipers = cliperService.searchClipers(query, pageable);
        Page<CliperDTO> cliperDTOs = clipers.map(CliperDTO::new);
        return ResponseEntity.ok(cliperDTOs);
    }

    @GetMapping("/by-skill")
    public ResponseEntity<List<CliperDTO>> getClipersBySkill(@RequestParam String skill) {
        List<Cliper> clipers = cliperService.findBySkill(skill);
        List<CliperDTO> cliperDTOs = clipers.stream()
                .map(CliperDTO::new)
                .toList();
        return ResponseEntity.ok(cliperDTOs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CliperDTO> updateCliper(
            @PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String description = request.get("description");

            Cliper updatedCliper = cliperService.updateCliper(id, title, description);
            return ResponseEntity.ok(new CliperDTO(updatedCliper));
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar cliper: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteCliper(@PathVariable String id) {
        try {
            cliperService.deleteCliper(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar cliper: " + e.getMessage(), e);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CliperDTO>> getClipersByStatus(@PathVariable String status) {
        try {
            Cliper.Status statusEnum = Cliper.Status.valueOf(status.toUpperCase());
            List<Cliper> clipers = cliperService.findByStatus(statusEnum);
            List<CliperDTO> cliperDTOs = clipers.stream()
                    .map(CliperDTO::new)
                    .toList();
            return ResponseEntity.ok(cliperDTOs);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + status, e);
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Obtener el email del JWT token y buscar el usuario real en la base de datos
        String email = auth.getName();
        // Buscar el usuario por email y devolver su ID real
        return userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    private String saveVideoFile(MultipartFile videoFile) {
        try {
            // Crear directorio si no existe
            Path uploadDir = Paths.get("./uploads/videos");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generar nombre único para el archivo
            String fileName = "video_" + System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            // Guardar el archivo
            Files.copy(videoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Devolver URL completa para que el frontend pueda acceder
            return fileUploadBaseUrl + "/uploads/videos/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo de video: " + e.getMessage());
        }
    }

    private Integer extractVideoDuration(MultipartFile videoFile) {
        // Simular extracción de duración
        // En producción, usaría FFmpeg para obtener la duración real
        // Generar duración entre 15-120 segundos (válido según validación)
        return 15 + (int)(Math.random() * 105); // 15-120 segundos
    }

    // Like/Unlike endpoints
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            Cliper cliper = cliperService.toggleLike(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("liked", cliper.isLikedBy(userId));
            response.put("likesCount", cliper.getLikesCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al dar like: " + e.getMessage(), e);
        }
    }

    // Comment endpoints
    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CliperDTO> addComment(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            String text = (String) request.get("text");
            
            // Get user name
            String userName = userRepository.findById(userId)
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .orElse("Usuario");
            
            Cliper cliper = cliperService.addComment(id, userId, userName, text);
            return ResponseEntity.ok(new CliperDTO(cliper));
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar comentario: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{cliperId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CliperDTO> deleteComment(
            @PathVariable String cliperId,
            @PathVariable String commentId) {
        try {
            String userId = getCurrentUserId();
            Cliper cliper = cliperService.deleteComment(cliperId, commentId, userId);
            return ResponseEntity.ok(new CliperDTO(cliper));
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar comentario: " + e.getMessage(), e);
        }
    }
}
