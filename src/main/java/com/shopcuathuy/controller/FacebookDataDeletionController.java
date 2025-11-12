package com.shopcuathuy.controller;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để xử lý Facebook Data Deletion Callback
 * Endpoint này được Facebook gọi khi người dùng yêu cầu xóa dữ liệu
 */
@RestController
@RequestMapping("/api/facebook")
@Slf4j
@CrossOrigin(origins = "*")
public class FacebookDataDeletionController {

    /**
     * Endpoint để xử lý yêu cầu xóa dữ liệu từ Facebook
     * Facebook sẽ gọi endpoint này với signed_request
     * 
     * @param signedRequest Signed request từ Facebook
     * @return Confirmation code cho Facebook
     */
    @PostMapping("/data-deletion")
    public ResponseEntity<Map<String, String>> handleDataDeletion(
            @RequestParam(value = "signed_request", required = false) String signedRequest) {
        
        log.info("Facebook data deletion request received");
        
        // Tạo confirmation code (Facebook yêu cầu)
        String confirmationCode = generateConfirmationCode();
        
        // TODO: Xử lý xóa dữ liệu người dùng ở đây
        // 1. Parse signed_request để lấy user_id
        // 2. Xóa dữ liệu người dùng trong database
        // 3. Trả về confirmation code
        
        Map<String, String> response = new HashMap<>();
        response.put("url", "https://yourdomain.com/deletion-status?id=" + confirmationCode);
        response.put("confirmation_code", confirmationCode);
        
        log.info("Data deletion confirmation code generated: {}", confirmationCode);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint GET để Facebook verify URL (optional)
     */
    @GetMapping("/data-deletion")
    public ResponseEntity<Map<String, String>> verifyDataDeletionEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Data deletion endpoint is active");
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo confirmation code duy nhất
     */
    private String generateConfirmationCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}

