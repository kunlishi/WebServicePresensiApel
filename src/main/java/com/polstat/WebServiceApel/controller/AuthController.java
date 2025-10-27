package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.*;
import com.polstat.WebServiceApel.service.AuthService;
import com.polstat.WebServiceApel.dto.AdminFirstRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autentikasi dan manajemen token JWT")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register (publik - MAHASISWA)", description = "Tidak perlu mengirim role. Otomatis menjadi MAHASISWA.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        return authService.register(request);

    }

    @Operation(summary = "Register Admin (publik)", description = "Membuat akun ADMIN pertama kali (tanpa kelas/tingkat). Opsional: name")
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminFirstRegisterRequest request){
        return authService.registerAdmin(request);
    }

    @Operation(summary = "Login (publik)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request){
        return authService.login(request);
    }

    // 🟢 VALIDATE TOKEN
    @Operation(summary = "Validasi token (publik)")
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody TokenValidationRequest request) {
        boolean valid = authService.validateToken(request.getToken());
        if (valid) {
            return ResponseEntity.ok("Token valid");
        } else {
            return ResponseEntity.status(401).body("Token tidak valid atau kedaluwarsa");
        }
    }

    // 🔐 LOGOUT (opsional) - stateless: klien cukup hapus tokennya
    @Operation(summary = "Logout (stateless)")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Silakan hapus token di sisi klien");
    }

    @Operation(summary = "Refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }
}
