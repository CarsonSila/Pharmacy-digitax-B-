package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.Banner;
import com.example.HeThongBanThuoc.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerControler {
    @Autowired
    private BannerService bannerService;

    // API - Lấy tất cả banner hiển thị (Interface Main home page)
    @GetMapping
    public ResponseEntity<?> getAllBanners() {
        List<Banner> result = bannerService.getAllActiveBanners();
        return ResponseEntity.ok(result);
    }
}
