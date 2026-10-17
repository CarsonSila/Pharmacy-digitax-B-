package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import com.example.HeThongBanThuoc.model.NguoiDung;
import com.example.HeThongBanThuoc.service.EmailService;
import com.example.HeThongBanThuoc.service.OTPService;
import com.example.HeThongBanThuoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // API - Gửi OTP quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        NguoiDung user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email không tồn tại"));
        }

        String otp = otpService.generateOTP(user);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of("message", "OTP đã được gửi tới email của bạn"));
    }

    // API - Xác thực OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        NguoiDung user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email không tồn tại"));
        }

        boolean validOtp = otpService.validateOTP(user, otp);
        if (!validOtp) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP không hợp lệ hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(Map.of("message", "Xác thực OTP thành công"));
    }

    // API - Đặt lại mật khẩu sau khi xác thực OTP
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu xác nhận không trùng khớp"));
        }

        NguoiDung user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email không tồn tại"));
        }

        userService.updatePassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
    }

    // API - Đổi mật khẩu khi đã đăng nhập
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu xác nhận không trùng khớp"));
        }

        // Lấy user hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Người dùng chưa đăng nhập"));
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        NguoiDung user = userService.findByEmail(principal.getEmail());
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Người dùng không tồn tại"));
        }

        if (!userService.checkPassword(user, currentPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu hiện tại không đúng"));
        }

        userService.updatePassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    // API - Kiểm tra độ mạnh cảu mật khẩu
    @PostMapping("/check-password-strength")
    public ResponseEntity<?> checkPasswordStrength(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu không được để trống"));
        }

        boolean length = password.length() >= 8;
        boolean uppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean lowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean number = password.chars().anyMatch(Character::isDigit);
        boolean specialChar = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        int score = 0;
        if (length) score++;
        if (uppercase) score++;
        if (lowercase) score++;
        if (number) score++;
        if (specialChar) score++;

        String strength;
        switch (score) {
            case 5 -> strength = "Mạnh";
            case 3, 4 -> strength = "Trung bình";
            default -> strength = "Yếu";
        }

        return ResponseEntity.ok(Map.of(
                "length", length,
                "uppercase", uppercase,
                "lowercase", lowercase,
                "number", number,
                "specialChar", specialChar,
                "strength", strength
        ));
    }
}
