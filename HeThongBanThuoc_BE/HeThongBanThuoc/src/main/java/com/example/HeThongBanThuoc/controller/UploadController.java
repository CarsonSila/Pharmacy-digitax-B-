package com.example.HeThongBanThuoc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final String UPLOAD_DIR = "D:/HeThongBanThuoc/uploads/";

    @PostMapping("/{type}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) {

        try {
            // Xác định thư mục
            String folderPath = UPLOAD_DIR + type + "/";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            // Lưu file
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(folderPath + fileName);
            Files.copy(file.getInputStream(), filePath);

            // Trả về đường dẫn tương đối
            String relativePath = "/uploads/" + type + "/" + fileName;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("path", relativePath);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi upload ảnh"));
        }
    }
}
