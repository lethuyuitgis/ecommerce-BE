package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateSellerRequestDTO;
import com.shopcuathuy.dto.request.UpdateSellerRequestDTO;
import com.shopcuathuy.dto.response.SellerResponseDTO;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public SellerController(SellerRepository sellerRepository,
                            UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerResponseDTO>> getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Optional<Seller> sellerOpt = sellerRepository.findByUserId(userId);
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Seller profile not found"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(sellerOpt.get())));
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<ApiResponse<SellerResponseDTO>> createSeller(
            @RequestBody(required = false) CreateSellerRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller existing = sellerRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Seller profile already exists", convertToDTO(existing)));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CreateSellerRequestDTO payload = request != null
            ? request
            : new CreateSellerRequestDTO();

        Seller seller = new Seller();
        seller.setUser(user);
        seller.setShopName(payload.shopName != null ? payload.shopName : user.getFullName());
        seller.setShopDescription(payload.shopDescription);
        seller.setShopPhone(payload.shopPhone);
        seller.setShopEmail(payload.shopEmail != null ? payload.shopEmail : user.getEmail());
        seller.setProvince(payload.province);
        seller.setDistrict(payload.district);

        Seller saved = sellerRepository.save(seller);

        if (user.getUserType() != User.UserType.SELLER) {
            user.setUserType(User.UserType.SELLER);
            userRepository.save(user);
        }

        return ResponseEntity.ok(ApiResponse.success("Seller profile created", convertToDTO(saved)));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<SellerResponseDTO>> updateProfile(
            @RequestBody(required = false) UpdateSellerRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        if (request == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Request body is required"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (request.shopName != null) seller.setShopName(request.shopName);
        if (request.shopDescription != null) seller.setShopDescription(request.shopDescription);
        if (request.shopPhone != null) seller.setShopPhone(request.shopPhone);
        if (request.shopEmail != null) seller.setShopEmail(request.shopEmail);
        if (request.province != null) seller.setProvince(request.province);
        if (request.district != null) seller.setDistrict(request.district);
        if (request.shopAvatar != null) seller.setShopAvatar(request.shopAvatar);
        if (request.shopCover != null) seller.setShopCover(request.shopCover);

        Seller updated = sellerRepository.save(seller);
        return ResponseEntity.ok(ApiResponse.success("Seller profile updated", convertToDTO(updated)));
    }

    private SellerResponseDTO convertToDTO(Seller seller) {
        SellerResponseDTO dto = new SellerResponseDTO();
        dto.id = seller.getId();
        dto.userId = seller.getUser() != null ? seller.getUser().getId() : null;
        dto.shopName = seller.getShopName();
        dto.shopDescription = seller.getShopDescription();
        dto.shopAvatar = seller.getShopAvatar();
        dto.shopCover = seller.getShopCover();
        dto.shopPhone = seller.getShopPhone();
        dto.shopEmail = seller.getShopEmail();
        dto.province = seller.getProvince();
        dto.district = seller.getDistrict();
        dto.verificationStatus = seller.getVerificationStatus() != null
            ? seller.getVerificationStatus().name()
            : null;
        dto.rating = seller.getRating() != null ? seller.getRating().doubleValue() : null;
        dto.totalProducts = seller.getTotalProducts();
        dto.totalFollowers = seller.getTotalFollowers();
        dto.totalOrders = seller.getTotalOrders();
        return dto;
    }
}

