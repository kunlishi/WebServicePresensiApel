package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.*;
import com.polstat.WebServiceApel.entity.User;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import com.polstat.WebServiceApel.repository.UserRepository;
import com.polstat.WebServiceApel.security.jwt.JwtService;
import com.polstat.WebServiceApel.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponse;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MahasiswaRepository mahasiswaRepository;


    public ResponseEntity<?> register(RegisterRequest request) {
        // Cek duplikasi username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username sudah digunakan");
        }

        // Register publik selalu sebagai MAHASISWA
        User.Role role = User.Role.MAHASISWA;

        // Simpan user baru
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .name(request.getNama())
                .build();

        userRepository.save(user);

        // Jika registrasi mahasiswa, buat record Mahasiswa tertaut
        if (role == User.Role.MAHASISWA) {
            Mahasiswa m = Mahasiswa.builder()
                    .nim(request.getUsername()) // nim = username
                    .nama(request.getNama() != null ? request.getNama() : request.getUsername())
                    .kelas(request.getKelas() != null ? request.getKelas() : "")
                    .tingkat(request.getTingkat())
                    .user(user)
                    .build();
            mahasiswaRepository.save(m);
        }

        // Buat token JWT
        String token = jwtService.generateToken(new CustomUserDetails(user));

        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .token(token)
                        .role(user.getRole().name())
                        .build()
        );
    }

    // Register Admin (publik) — hanya bila belum ada ADMIN sama sekali
    public ResponseEntity<?> registerAdmin(AdminFirstRegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username sudah digunakan");
        }
        // Endpoint ini selalu membuat ADMIN
        // Izinkan hanya jika belum ada ADMIN di sistem
        boolean adminExists = userRepository.findAll().stream().anyMatch(u -> u.getRole() == User.Role.ADMIN);
        if (adminExists) {
            return ResponseEntity.status(403).body("Admin sudah ada. Gunakan akun admin untuk membuat user tambahan");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ADMIN)
                .name(request.getName() != null && !request.getName().isBlank() ? request.getName() : "Admin")
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(new CustomUserDetails(user));
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .token(token)
                        .role(user.getRole().name())
                        .build()
        );
    }
    public ResponseEntity<?> login(AuthenticationRequest request) {
        // Proses autentikasi
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Ambil detail user dari hasil autentikasi
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // Kembalikan response seperti di AuthController
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .token(token)
                        .role(userDetails.getUser().getRole().name())
                        .build()
        );
    }

    // 🟢 VALIDATE TOKEN
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        String oldToken = request.getToken();
        if (!jwtService.validateToken(oldToken)) {
            return ResponseEntity.status(401).body("Token tidak valid");
        }
        String username = jwtService.extractUsername(oldToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User tidak ditemukan");
        }
        CustomUserDetails userDetails = new CustomUserDetails(userOpt.get());
        String newToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(newToken)
                .role(userOpt.get().getRole().name())
                .build());
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Cek apakah password lama cocok
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Password lama salah!");
        }

        // Update dengan password baru yang sudah di-encode
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
