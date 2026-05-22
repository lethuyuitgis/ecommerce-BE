package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Banner;
import com.shopcuathuy.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Banner>>> getBanners(
            @RequestParam(defaultValue = "HOME_MAIN") String position) {
        return ResponseEntity.ok(ApiResponse.success(
            bannerRepository.findByPositionAndIsActiveTrueOrderByDisplayOrderAsc(position)
        ));
    }

    /* Admin endpoints */
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<Banner>> createBanner(@RequestBody Banner banner) {
        banner.setId(UUID.randomUUID().toString());
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.save(banner)));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Banner>> updateBanner(@PathVariable String id, @RequestBody Banner banner) {
        banner.setId(id);
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.save(banner)));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable String id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
