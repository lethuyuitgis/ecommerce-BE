package com.shopcuathuy.service;

import com.shopcuathuy.config.MinIOConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;

@Service
public class MinIOService {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    @Autowired
    public MinIOService(MinioClient minioClient, MinIOConfig minIOConfig) {
        this.minioClient = minioClient;
        this.minIOConfig = minIOConfig;
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minIOConfig.getBucketName())
                .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .build());
            }
        } catch (Exception e) {
            // Log error but don't fail startup
            System.err.println("Warning: Could not create MinIO bucket: " + e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            String objectName = folder != null && !folder.isEmpty() ? folder + "/" + filename : filename;

            // Upload to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // Return proxy URL that works with Next.js
            return getProxyUrl(objectName);
        } catch (MinioException e) {
            throw new Exception("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    public String uploadImage(MultipartFile file) throws Exception {
        return uploadFile(file, "images");
    }

    public String getFileUrl(String objectName) {
        // Return proxy URL through backend instead of direct MinIO URL
        // This avoids needing to set bucket public
        // Frontend will use Next.js proxy to access this
        return minIOConfig.getUrl() + "/" + minIOConfig.getBucketName() + "/" + objectName;
    }
    
    public String getProxyUrl(String objectName) {
        // Return URL that goes through Next.js proxy
        // Format: /api/upload/image/{objectName}
        return "/api/upload/image/" + objectName;
    }

    public InputStream getFile(String objectName) throws Exception {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .object(objectName)
                    .build()
            );
        } catch (MinioException e) {
            throw new Exception("Failed to get file from MinIO: " + e.getMessage(), e);
        }
    }
}

