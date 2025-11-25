package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.service.MinIOService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UploadController {

    private final MinIOService minIOService;

    @Autowired
    public UploadController(MinIOService minIOService) {
        this.minIOService = minIOService;
    }

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            MultipartFile uploadFile = null;
            
            // Try to get file from request parameters first
            if (file != null && !file.isEmpty()) {
                uploadFile = file;
            } else if (image != null && !image.isEmpty()) {
                uploadFile = image;
            } else if (request instanceof MultipartHttpServletRequest) {
                // If not found in params, try to find any file part in the request
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                Iterator<String> fileNames = multipartRequest.getFileNames();
                if (fileNames.hasNext()) {
                    String fileName = fileNames.next();
                    uploadFile = multipartRequest.getFile(fileName);
                }
            }
            
            // Validate file
            if (uploadFile == null || uploadFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is required. Please provide 'file' or 'image' field in multipart/form-data."));
            }

            // Validate file type - allow both image and video
            String contentType = uploadFile.getContentType();
            boolean isImage = contentType != null && contentType.startsWith("image/");
            boolean isVideo = contentType != null && contentType.startsWith("video/");
            
            if (!isImage && !isVideo) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be an image or video"));
            }

            // Validate file size
            long maxSize = isVideo ? 100 * 1024 * 1024 : 10 * 1024 * 1024; // 100MB for video, 10MB for image
            if (uploadFile.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(isVideo ? "File size must be less than 100MB" : "File size must be less than 10MB"));
            }
            
            // Determine folder based on file type
            String uploadFolder = isVideo ? "videos" : "images";

            // Upload to MinIO
            String fileUrl = minIOService.uploadFile(uploadFile, uploadFolder);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileUrl", fileUrl); // Support both "url" and "fileUrl" for compatibility
            result.put("filename", uploadFile.getOriginalFilename());
            result.put("type", isVideo ? "video" : "image");

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/video")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadVideo(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "video", required = false) MultipartFile video) {
        try {
            MultipartFile uploadFile = null;
            
            // Try to get file from request parameters first
            if (file != null && !file.isEmpty()) {
                uploadFile = file;
            } else if (video != null && !video.isEmpty()) {
                uploadFile = video;
            } else if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                Iterator<String> fileNames = multipartRequest.getFileNames();
                if (fileNames.hasNext()) {
                    String fileName = fileNames.next();
                    uploadFile = multipartRequest.getFile(fileName);
                }
            }
            
            // Validate file
            if (uploadFile == null || uploadFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is required. Please provide 'file' or 'video' field in multipart/form-data."));
            }

            // Validate file type - must be video
            String contentType = uploadFile.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be a video"));
            }

            // Validate file size (max 100MB for videos)
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (uploadFile.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must be less than 100MB"));
            }

            // Upload to MinIO
            String videoUrl = minIOService.uploadFile(uploadFile, "videos");

            Map<String, String> result = new HashMap<>();
            result.put("url", videoUrl);
            result.put("fileUrl", videoUrl);
            result.put("filename", uploadFile.getOriginalFilename());

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to upload video: " + e.getMessage()));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            HttpServletRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "") String folder) {
        try {
            MultipartFile uploadFile = null;
            
            // Try to get file from request parameters first
            if (file != null && !file.isEmpty()) {
                uploadFile = file;
            } else if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                Iterator<String> fileNames = multipartRequest.getFileNames();
                if (fileNames.hasNext()) {
                    String fileName = fileNames.next();
                    uploadFile = multipartRequest.getFile(fileName);
                }
            }
            
            // Validate file
            if (uploadFile == null || uploadFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is required. Please provide 'file' field in multipart/form-data."));
            }

            // Validate file size (max 50MB)
            long maxSize = 50 * 1024 * 1024; // 50MB
            if (uploadFile.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must be less than 50MB"));
            }

            // Upload to MinIO
            String fileUrl = minIOService.uploadFile(uploadFile, folder);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileUrl", fileUrl);
            result.put("filename", uploadFile.getOriginalFilename());

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/image/**")
    public ResponseEntity<byte[]> getImage(HttpServletRequest request) {
        try {
            // Extract path from request URI
            String requestUri = request.getRequestURI();
            // Remove /api/upload/image prefix
            String path = requestUri.replace("/api/upload/image/", "");
            
            // Get file from MinIO
            InputStream fileStream = minIOService.getFile(path);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] fileBytes = buffer.toByteArray();
            fileStream.close();
            
            // Determine content type from file extension
            String contentType = "image/jpeg";
            String lowerPath = path.toLowerCase();
            if (lowerPath.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerPath.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerPath.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lowerPath.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileBytes.length);
            headers.setCacheControl("public, max-age=31536000"); // Cache for 1 year
            headers.set("Access-Control-Allow-Origin", "*");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
        } catch (Exception e) {
            System.err.println("Error serving image: " + e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }
}

