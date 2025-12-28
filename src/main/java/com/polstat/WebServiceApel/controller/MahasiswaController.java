package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.MahasiswaResponse;
import com.polstat.WebServiceApel.dto.PresensiRecordResponse;
import com.polstat.WebServiceApel.service.MahasiswaService;
import com.polstat.WebServiceApel.service.PresensiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/mahasiswa")
@Tag(name = "Mahasiswa", description = "Profil dan pencarian data mahasiswa")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MahasiswaController {
    private final MahasiswaService mahasiswaService;
    private final PresensiService presensiService;

    @Operation(summary = "Profil mahasiswa saya", description = "Role: MAHASISWA. Profil mahasiswa berdasarkan user login")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MahasiswaResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok(mahasiswaService.getProfileByUsername(username));
    }

    @Operation(summary = "List semua mahasiswa", description = "Role: ADMIN atau SPD. Admin/SPD dapat melihat seluruh mahasiswa")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SPD')")
    public ResponseEntity<java.util.List<MahasiswaResponse>> listMahasiswa() {
        return ResponseEntity.ok(mahasiswaService.findAll());
    }

    @Operation(summary = "Detail mahasiswa berdasarkan NIM", description = "Role: ADMIN atau SPD. Admin/SPD mencari mahasiswa via NIM")
    @GetMapping("/{nim}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SPD')")
    public ResponseEntity<MahasiswaResponse> getByNim(@PathVariable String nim) {
        return ResponseEntity.ok(mahasiswaService.findByNim(nim));
    }

    @Operation(summary = "List mahasiswa per tingkat", description = "Role: ADMIN atau SPD. Admin/SPD menampilkan mahasiswa berdasarkan tingkat")
    @GetMapping("/kelas/{tingkat}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SPD')")
    public ResponseEntity<java.util.List<MahasiswaResponse>> listByTingkat(@PathVariable String tingkat) {
        return ResponseEntity.ok(mahasiswaService.findByTingkat(tingkat));
    }

    @Operation(summary = "Riwayat presensi saya", description = "Role: MAHASISWA. Melihat daftar kehadiran diri sendiri")
    @GetMapping("/riwayat")
    @PreAuthorize("hasAuthority('MAHASISWA')")
    public ResponseEntity<List<PresensiRecordResponse>> getRiwayat() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // Mengambil NIM dari token JWT
        return ResponseEntity.ok(presensiService.getRiwayatByNim(username));
    }
}
