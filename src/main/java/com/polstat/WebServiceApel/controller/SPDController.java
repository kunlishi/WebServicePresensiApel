package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.*;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.service.PresensiService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/spd")
@Tag(name = "SPD", description = "Manajemen presensi dan keterlambatan oleh SPD")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SPDController {
    private final PresensiService presensiService;

    @Operation(summary = "Upload presensi", description = "Role: SPD. Mengunggah NIM hadir untuk suatu jadwal")
    @PostMapping("/presensi")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<PresensiRecordResponse> uploadPresensi(@RequestBody PresensiRequest request) {
        return ResponseEntity.ok(presensiService.savePresensiSingle(request));
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
    public ResponseEntity<PresensiRecordResponse> markTerlambat(@RequestBody PresensiRequest request) {
        return ResponseEntity.ok(presensiService.markTerlambatSingle(request));
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

    // Endpoint baru untuk Real-time Scanning
    @Operation(summary = "Proses Scan QR", description = "Role: SPD. Memproses scan QR Mahasiswa dengan logika toleransi waktu")
    @PostMapping("/scan")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<ScanResponse> scan(@org.springframework.web.bind.annotation.RequestParam String nim,
                                             @org.springframework.web.bind.annotation.RequestParam Long scheduleId) {
        return ResponseEntity.ok(presensiService.processScan(nim, scheduleId));
    }

    // Endpoint baru untuk Konfirmasi Manual Status
    @Operation(summary = "Konfirmasi Status Manual", description = "Role: SPD. Menentukan status HADIR/TERLAMBAT saat di masa transisi")
    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<ScanResponse> confirm(
            @RequestParam String nim,
            @RequestParam Long scheduleId,
            @RequestParam String status) {

        presensiService.confirmManual(nim, scheduleId, status);

        // Kirim objek JSON, bukan String, agar Android tidak error parsing
        return ResponseEntity.ok(ScanResponse.builder()
                .status(status)
                .message("Konfirmasi Berhasil")
                .nim(nim)
                .build());
    }

    @Operation(summary = "Daftar jadwal untuk SPD", description = "Mengambil jadwal berdasarkan tanggal untuk dipilih sebelum scanning")
    @GetMapping("/schedules")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<List<ApelSchedule>> getSchedulesForSpd(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(presensiService.getSchedulesByDate(targetDate));
    }

    @Operation(summary = "Riwayat per jadwal", description = "Melihat siapa saja yang sudah di-scan untuk jadwal ini")
    @GetMapping("/schedules/{scheduleId}/presensi")
    @PreAuthorize("hasAuthority('SPD')")
    public ResponseEntity<List<PresensiRecordResponse>> getHistoryBySchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(presensiService.getHistoryByScheduleId(scheduleId));
    }
}
