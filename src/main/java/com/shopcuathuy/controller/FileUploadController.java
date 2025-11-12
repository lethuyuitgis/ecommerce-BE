package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.service.MinIOService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    private final MinIOService minIOService;
    
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "images") String folder) {
        String objectName = minIOService.uploadFile(file, folder);
        String fileUrl = minIOService.getFileUrl(objectName);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", Map.of(
            "fileUrl", fileUrl,
            "objectName", objectName
        )));
    }
    
    @DeleteMapping("/image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("url") String url) {
        minIOService.deleteFile(url);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}

