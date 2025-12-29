package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.JadwalApelRequest;
import com.polstat.WebServiceApel.dto.PresensiRecordResponse;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.IzinSakit;
import com.polstat.WebServiceApel.service.AdminService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Manajemen jadwal, izin, dan presensi oleh admin")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Buat jadwal apel", description = "Role: ADMIN. Admin membuat jadwal apel baru")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JadwalApelRequest.class),
                    examples = @ExampleObject(
                            name = "Contoh JadwalApelRequest",
                            value = "{\n  \"tanggal\": \"2025-10-27\",\n  \"waktu\": \"07:00:00\",\n  \"tingkat\": \"3\",\n  \"keterangan\": \"Apel tingkat 3\"\n}"
                    )
            )
    )
    @PostMapping("/jadwal")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Long> createJadwal(@RequestBody JadwalApelRequest request) {
        return ResponseEntity.ok(adminService.createJadwal(request));
    }

    @Operation(summary = "Validasi izin", description = "Role: ADMIN. Admin menyetujui/menolak izin mahasiswa")
    @PutMapping("/izin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> validateIzin(@PathVariable Long id, @RequestParam("status") IzinSakit.Status status) {
        adminService.validateIzin(id, status);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hapus semua bukti", description = "Role: ADMIN. Admin menghapus seluruh file bukti izin")
    @DeleteMapping("/bukti")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Long> clearAllBukti() {
        return ResponseEntity.ok(adminService.clearAllBukti());
    }

    @Operation(summary = "List jadwal", description = "Role: ADMIN. Admin melihat seluruh jadwal apel")
    @GetMapping("/jadwal")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ApelSchedule>> listJadwal() {
        return ResponseEntity.ok(adminService.listJadwal());
    }

    @Operation(summary = "Update jadwal", description = "Role: ADMIN. Admin memperbarui jadwal apel berdasarkan ID")
    @PutMapping("/jadwal/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateJadwal(@PathVariable Long id, @RequestBody JadwalApelRequest request) {
        adminService.updateJadwal(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hapus jadwal", description = "Role: ADMIN. Admin menghapus jadwal apel berdasarkan ID")
    @DeleteMapping("/jadwal/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteJadwal(@PathVariable Long id) {
        adminService.deleteJadwal(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List pengajuan izin", description = "Role: ADMIN. Admin melihat seluruh pengajuan izin mahasiswa")
    @GetMapping("/izin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<com.polstat.WebServiceApel.dto.AdminIzinSakitSummary>> listIzin() {
        return ResponseEntity.ok(adminService.listIzin());
    }

    @Operation(summary = "List presensi", description = "Role: ADMIN. Admin melihat seluruh data presensi")
    @GetMapping("/presensi")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<com.polstat.WebServiceApel.dto.PresensiRecordResponse>> listPresensiAll() {
        return ResponseEntity.ok(adminService.listPresensiAll());
    }

    @Operation(summary = "Rekap presensi per jadwal", description = "Role: ADMIN. Melihat daftar mahasiswa yang hadir/tidak pada jadwal tertentu")
    @GetMapping("/presensi/rekap/{scheduleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PresensiRecordResponse>> getRekapByJadwal(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(adminService.getFullRekapByJadwal(scheduleId));
    }

    // Endpoint unduh bukti dihapus karena list sudah menyertakan buktiBase64
}
