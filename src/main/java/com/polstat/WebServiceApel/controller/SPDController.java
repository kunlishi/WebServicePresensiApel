package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.PresensiBatchRequest;
import com.polstat.WebServiceApel.dto.PresensiBatchResponse;
import com.polstat.WebServiceApel.dto.PresensiRecordResponse;
import com.polstat.WebServiceApel.dto.TerlambatRequest;
import com.polstat.WebServiceApel.service.PresensiService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/spd")
@Tag(name = "SPD", description = "Manajemen presensi dan keterlambatan oleh SPD")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SPDController {
    private final PresensiService presensiService;

    @Operation(summary = "Upload presensi batch", description = "Role: SPD. Mengunggah daftar NIM hadir untuk suatu jadwal")
    @PostMapping("/presensi")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<PresensiBatchResponse> uploadPresensi(@RequestBody PresensiBatchRequest request) {
        return ResponseEntity.ok(presensiService.saveBatch(request));
    }

    @Operation(summary = "Rekap presensi", description = "Role: SPD. Melihat data presensi, filter opsional tanggal/tingkat")
    @GetMapping("/presensi")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<?> listPresensi(
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.LocalDate tanggal,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String tingkat
    ) {
        return ResponseEntity.ok(presensiService.listPresensi(tanggal, tingkat));
    }

    @Operation(summary = "Rekap presensi per tanggal", description = "Role: SPD. Melihat presensi pada tanggal tertentu")
    @GetMapping("/presensi/{tanggal}")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<?> listPresensiByTanggal(@PathVariable java.time.LocalDate tanggal,
                                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String tingkat) {
        return ResponseEntity.ok(presensiService.listPresensi(tanggal, tingkat));
    }

    @Operation(summary = "Hapus entri presensi", description = "Role: SPD. Menghapus entri presensi yang salah")
    @DeleteMapping("/presensi/{id}")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<Void> deletePresensi(@PathVariable Long id) {
        presensiService.deletePresensiById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Tandai terlambat batch", description = "Role: SPD. Menandai mahasiswa terlambat untuk jadwal tertentu")
    @PostMapping("/terlambat")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<Long> markTerlambat(@RequestBody TerlambatRequest request) {
        long updated = presensiService.markTerlambat(request.getTanggal(), request.getTingkat(), request.getMahasiswa());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Rekap terlambat", description = "Role: SPD. Melihat ringkasan keterlambatan, filter opsional tanggal/tingkat")
    @GetMapping("/terlambat")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<?> listTerlambat(
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.time.LocalDate tanggal,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String tingkat
    ) {
        java.util.List<PresensiRecordResponse> all = presensiService.listPresensi(tanggal, tingkat);
        return ResponseEntity.ok(all.stream().filter(p -> p.getStatus() == com.polstat.WebServiceApel.entity.Presensi.Status.TERLAMBAT).toList());
    }
}
