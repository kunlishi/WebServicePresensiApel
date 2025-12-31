package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.entity.User;
import com.polstat.WebServiceApel.repository.UserRepository;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import com.polstat.WebServiceApel.repository.PresensiRepository;
import com.polstat.WebServiceApel.repository.IzinSakitRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Manajemen akun user dan admin")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MahasiswaRepository mahasiswaRepository;
    private final PresensiRepository presensiRepository;
    private final IzinSakitRepository izinSakitRepository;

    @Operation(summary = "Profil saya", description = "Role: LOGIN. Mengambil profil user yang sedang login")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> profile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "name", user.getName()
        ));
    }

    @Operation(summary = "Update profil saya", description = "Role: LOGIN. Memperbarui data profil user yang sedang login")
    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(@RequestBody UpdateProfileRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            user.setUsername(req.getUsername());
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            user.setName(req.getName());
        }
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ganti password", description = "Role: LOGIN. Mengganti password user yang sedang login")
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        // Verifikasi password lama
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            // Mengembalikan pesan error agar UI Android bisa menampilkannya
            return ResponseEntity.status(400).body(Map.of("message", "Password lama tidak sesuai"));
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password berhasil diubah"));
    }

    @Operation(summary = "Hapus akun saya", description = "Role: LOGIN. Menghapus akun user yang sedang login")
    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Void> deleteAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        mahasiswaRepository.findByUser_Username(username).ifPresent(m -> {
            // Hapus entitas anak terlebih dahulu untuk memenuhi constraint FK
            izinSakitRepository.deleteByMahasiswa(m);
            presensiRepository.deleteByMahasiswa(m);
            mahasiswaRepository.delete(m);
        });
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Register oleh admin", description = "Role: ADMIN. Admin membuat akun baru untuk role ADMIN/SPD")
    @PostMapping("/admin/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> registerByAdmin(@RequestBody AdminRegisterRequest req) {
        User.Role role = User.Role.valueOf(req.getRole().toUpperCase());
        if (role == User.Role.MAHASISWA) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .name(req.getName() != null && !req.getName().isBlank() ? req.getName() : (role == User.Role.ADMIN ? "Admin" : "SPD"))
                .build();
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class UpdateProfileRequest { private String username; private String name; }
    @Data
    public static class ChangePasswordRequest { private String oldPassword; private String newPassword; }
    @Data
    public static class AdminRegisterRequest { private String username; private String password; private String role; private String name; }

    // Administratif: kelola semua user
    @Operation(summary = "List semua user", description = "Role: ADMIN. Admin melihat seluruh akun user")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("role", u.getRole().name());
                    m.put("name", u.getName());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Detail user", description = "Role: ADMIN. Admin melihat detail user berdasarkan ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow();
        Map<String, Object> body = new HashMap<>();
        body.put("id", u.getId());
        body.put("username", u.getUsername());
        body.put("role", u.getRole().name());
        body.put("name", u.getName());
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Hapus user", description = "Role: ADMIN. Admin menghapus akun user berdasarkan ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Cari Mahasiswa yang terhubung ke user ini (jika ada) lalu hapus dependensinya
        mahasiswaRepository.findByUser_Username(user.getUsername()).ifPresent(m -> {
            izinSakitRepository.deleteByMahasiswa(m);
            presensiRepository.deleteByMahasiswa(m);
            mahasiswaRepository.delete(m);
        });
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // Endpoint ubah role user dihapus sesuai permintaan
}
