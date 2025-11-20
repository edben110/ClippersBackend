package com.clipers.clipers.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving static files (images, videos, avatars)
 * Public access - no authentication required
 */
@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = "*")
public class StaticFileController {

    private static final String UPLOAD_DIRECTORY = "./uploads/";

    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String type,
            @PathVariable String filename) {
        
        try {
            // Validate type (avatars, images, videos, thumbnails)
            if (!isValidType(type)) {
                return ResponseEntity.badRequest().build();
            }

            // Decode filename and construct path
            String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
            Path filePath = Paths.get(UPLOAD_DIRECTORY + type + "/" + decodedFilename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            File file = filePath.toFile();
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(new FileSystemResource(file));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isValidType(String type) {
        return type.equals("avatars") || 
               type.equals("images") || 
               type.equals("videos") || 
               type.equals("thumbnails");
    }
}
