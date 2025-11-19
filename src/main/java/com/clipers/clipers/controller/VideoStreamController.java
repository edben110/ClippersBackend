package com.clipers.clipers.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for streaming video files with HTTP Range Request support
 * Optimized for mobile devices and large video files
 */
@RestController
@RequestMapping("/api/stream")
@CrossOrigin(origins = "*")
public class VideoStreamController {

    private static final String VIDEO_DIRECTORY = "./uploads/videos/";
    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB chunks

    /**
     * Stream video with Range Request support
     * Supports partial content delivery for better mobile experience
     */
    @GetMapping("/video/{filename:.+}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        
        try {
            // Decode URL-encoded filename (handles spaces and special characters)
            String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");
            Path videoPath = Paths.get(VIDEO_DIRECTORY + decodedFilename);
            
            System.out.println("Streaming video request: " + decodedFilename);
            System.out.println("Full path: " + videoPath.toAbsolutePath());
            
            if (!Files.exists(videoPath)) {
                System.err.println("Video file not found: " + videoPath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            File videoFile = videoPath.toFile();
            long fileSize = videoFile.length();
            
            // Determine content type
            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "video/mp4"; // Default to mp4
            }

            // If no range header, return full video
            if (rangeHeader == null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(new FileSystemResource(videoFile));
            }

            // Parse range header
            HttpRange range = HttpRange.parseRanges(rangeHeader).get(0);
            long start = range.getRangeStart(fileSize);
            long end = range.getRangeEnd(fileSize);
            long contentLength = end - start + 1;

            // Read the requested range
            byte[] data = readByteRange(videoFile, start, end);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(new org.springframework.core.io.ByteArrayResource(data));

        } catch (Exception e) {
            System.err.println("Error streaming video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Read a specific byte range from a file
     */
    private byte[] readByteRange(File file, long start, long end) throws IOException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long rangeLength = end - start + 1;
            byte[] buffer = new byte[(int) rangeLength];
            randomAccessFile.seek(start);
            randomAccessFile.readFully(buffer);
            return buffer;
        }
    }
}
