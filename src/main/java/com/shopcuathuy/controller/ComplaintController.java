package com.shopcuathuy.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminComplaintDTO;
import com.shopcuathuy.admin.dto.CreateComplaintRequest;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateComplaintMessageRequestDTO;
import com.shopcuathuy.dto.request.CreateComplaintRequestDTO;
import com.shopcuathuy.dto.response.ComplaintMessageResponseDTO;
import com.shopcuathuy.dto.response.ComplaintResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ComplaintController {

    private final AdminService adminService;

    public ComplaintController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponseDTO>>> listComplaints(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String status
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        List<ComplaintResponseDTO> responses = adminService.listComplaintsByReporter(userId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> getComplaint(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        return adminService.getComplaint(id)
                .filter(c -> userId.equals(c.getReporterId()))
                .map(this::convertToDTO)
                .map(ApiResponse::success)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(ApiResponse.error("Complaint not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> createComplaint(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateComplaintRequestDTO request
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        if (request.title == null || request.title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Title is required"));
        }
        if (request.description == null || request.description.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Description is required"));
        }

        CreateComplaintRequest createRequest = new CreateComplaintRequest();
        createRequest.setReporterId(userId);
        createRequest.setTargetId(request.sellerId);
        createRequest.setCategory(request.category == null ? "RETURN" : request.category.toUpperCase());
        createRequest.setTitle(request.title);
        createRequest.setContent(request.description);
        createRequest.setStatus("PENDING");
        createRequest.setOrderId(request.orderId);
        createRequest.setProductId(request.productId);
        createRequest.setDesiredResolution(request.desiredResolution);

        AdminComplaintDTO created = adminService.createComplaint(createRequest);
        return ResponseEntity.ok(ApiResponse.success(convertToDTO(created)));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<ComplaintMessageResponseDTO>>> getMessages(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }
        boolean ownsComplaint = adminService.getComplaint(id)
            .map(c -> userId.equals(c.getReporterId()))
            .orElse(false);
        if (!ownsComplaint) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        List<ComplaintMessageResponseDTO> messages = adminService.listComplaintMessages(id);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ComplaintMessageResponseDTO>> createMessage(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateComplaintMessageRequestDTO request
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }
        if (request == null || request.content == null || request.content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Content is required"));
        }
        boolean ownsComplaint = adminService.getComplaint(id)
            .map(c -> userId.equals(c.getReporterId()))
            .orElse(false);
        if (!ownsComplaint) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        return adminService.addComplaintMessage(id, userId, "CUSTOMER", request.content, request.attachments)
            .map(ApiResponse::success)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Complaint not found")));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> cancelComplaint(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }
        return adminService.cancelComplaint(id, userId)
            .map(this::convertToDTO)
            .map(ApiResponse::success)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Complaint not found")));
    }

    private ComplaintResponseDTO convertToDTO(AdminComplaintDTO dto) {
        ComplaintResponseDTO response = new ComplaintResponseDTO();
        response.id = dto.getId();
        response.orderId = dto.getOrderId();
        response.productId = dto.getProductId();
        response.sellerId = dto.getTargetId();
        response.category = dto.getCategory();
        response.title = dto.getTitle();
        response.description = dto.getContent();
        response.status = dto.getStatus();
        response.desiredResolution = dto.getDesiredResolution();
        response.createdAt = dto.getCreatedAt();
        response.updatedAt = dto.getUpdatedAt();
        return response;
    }
}

