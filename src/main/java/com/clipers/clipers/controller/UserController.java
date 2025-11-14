package com.clipers.clipers.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.multipart.MultipartFile;

import com.clipers.clipers.entity.User;
import com.clipers.clipers.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<User> updateProfile(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== UPDATE PROFILE REQUEST ===");
            System.out.println("Request data: " + request);
            
            String userId = getCurrentUserId();
            System.out.println("User ID: " + userId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            System.out.println("User found: " + user.getEmail());

            // Update basic fields
            if (request.containsKey("firstName")) {
                user.setFirstName((String) request.get("firstName"));
                System.out.println("Updated firstName: " + user.getFirstName());
            }
            if (request.containsKey("lastName")) {
                user.setLastName((String) request.get("lastName"));
                System.out.println("Updated lastName: " + user.getLastName());
            }
            if (request.containsKey("profileImage")) {
                user.setProfileImage((String) request.get("profileImage"));
                System.out.println("Updated profileImage: " + user.getProfileImage());
            }
            if (request.containsKey("phone")) {
                user.setPhone((String) request.get("phone"));
                System.out.println("Updated phone: " + user.getPhone());
            }
            if (request.containsKey("address")) {
                user.setAddress((String) request.get("address"));
                System.out.println("Updated address: " + user.getAddress());
            }

            User savedUser = userRepository.save(user);
            System.out.println("User saved successfully");
            System.out.println("=== END UPDATE PROFILE ===");
            
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar perfil: " + e.getMessage(), e);
        }
    }

    @PostMapping("/upload/avatar")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Create uploads directory if it doesn't exist
            Path uploadDir = Paths.get("./uploads/avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Sanitize filename - remove spaces and special characters
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = originalFilename != null 
                ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") 
                : "avatar.png";
            
            // Generate unique filename
            String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + "_" + sanitizedFilename;
            Path filePath = uploadDir.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user profile image
            String imageUrl = "/uploads/avatars/" + fileName;
            user.setProfileImage(imageUrl);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir avatar: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/profile/avatar")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, String>> deleteAvatar() {
        try {
            String userId = getCurrentUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Delete old avatar file if exists
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get("./uploads/avatars", user.getProfileImage().substring(user.getProfileImage().lastIndexOf('/') + 1));
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    // Log but don't fail the operation
                    System.err.println("Error deleting old avatar file: " + e.getMessage());
                }
            }

            // Clear profile image
            user.setProfileImage(null);
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar avatar: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}