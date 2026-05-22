package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.service.MinIOService;
import io.minio.messages.Item;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
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

    /* ===================== Excel files ===================== */

    private static final String EXCEL_FOLDER = "excel";

    @PostMapping("/excel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File is required"));
            }
            String name = file.getOriginalFilename();
            if (name == null || !(name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File must be .xlsx or .xls"));
            }
            long maxSize = 50 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File size must be less than 50MB"));
            }
            String fileUrl = minIOService.uploadFile(file, EXCEL_FOLDER);
            String objectName = fileUrl.replace("/api/upload/image/", "");
            String storedFileName = objectName.startsWith(EXCEL_FOLDER + "/")
                ? objectName.substring(EXCEL_FOLDER.length() + 1)
                : objectName;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("fileName", storedFileName);
            body.put("originalName", file.getOriginalFilename());
            body.put("filePath", objectName);
            body.put("fileUrl", fileUrl);
            body.put("size", file.getSize());
            body.put("mimeType", file.getContentType());
            body.put("uploadedAt", java.time.Instant.now());
            body.put("userId", userId);
            return ResponseEntity.ok(ApiResponse.success(body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to upload excel: " + e.getMessage()));
        }
    }

    @GetMapping("/excel/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listExcel(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            List<Item> all = minIOService.listObjects(EXCEL_FOLDER + "/");
            List<Map<String, Object>> files = new ArrayList<>();
            for (Item item : all) {
                String objectName = item.objectName();
                String fileName = objectName.startsWith(EXCEL_FOLDER + "/")
                    ? objectName.substring(EXCEL_FOLDER.length() + 1)
                    : objectName;
                Map<String, Object> f = new LinkedHashMap<>();
                f.put("fileName", fileName);
                f.put("fileUrl", "/api/upload/image/" + objectName);
                f.put("size", item.size());
                java.time.Instant ts = item.lastModified() != null
                    ? item.lastModified().toInstant() : java.time.Instant.now();
                f.put("uploadedAt", ts);
                f.put("modifiedAt", ts);
                files.add(f);
            }
            int total = files.size();
            List<Map<String, Object>> paged = files.stream()
                .skip(offset).limit(limit).toList();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("files", paged);
            body.put("total", total);
            body.put("limit", limit);
            body.put("offset", offset);
            return ResponseEntity.ok(ApiResponse.success(body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to list excel files: " + e.getMessage()));
        }
    }

    @GetMapping("/excel/{fileName}")
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String fileName) {
        try {
            String objectName = EXCEL_FOLDER + "/" + fileName;
            try (InputStream in = minIOService.getFile(objectName)) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = in.read(chunk)) != -1) buf.write(chunk, 0, n);
                byte[] data = buf.toByteArray();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(data.length);
                return ResponseEntity.ok().headers(headers).body(data);
            }
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/excel/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteExcel(@PathVariable String fileName) {
        try {
            minIOService.removeObject(EXCEL_FOLDER + "/" + fileName);
            return ResponseEntity.ok(ApiResponse.success("Deleted: " + fileName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to delete: " + e.getMessage()));
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

