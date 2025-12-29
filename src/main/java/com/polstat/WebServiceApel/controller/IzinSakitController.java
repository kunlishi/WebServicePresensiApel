package com.polstat.WebServiceApel.controller;

import com.polstat.WebServiceApel.dto.IzinSakitRequest;
import com.polstat.WebServiceApel.dto.IzinSakitDetailResponse;
import com.polstat.WebServiceApel.service.IzinSakitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/mahasiswa/izin")
@Tag(name = "Izin Mahasiswa", description = "Pengajuan dan pelacakan izin/sakit oleh mahasiswa")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class IzinSakitController {
    private final IzinSakitService izinSakitService;

    @Operation(summary = "Ajukan izin/sakit (multipart)", description = "Mahasiswa mengajukan izin/sakit dengan upload file bukti (multipart/form-data)")
    @PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Long> ajukanMultipart(
            @RequestParam("tanggal") java.time.LocalDate tanggal,
            @RequestParam(value = "tingkat", required = true) String tingkat,
            @RequestParam("jenis") String jenis,
            @RequestParam("keterangan") String keterangan,
            @RequestParam(value = "bukti", required = true) MultipartFile bukti
    ) throws java.io.IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] buktiBytes = (bukti != null && !bukti.isEmpty()) ? bukti.getBytes() : null;

        // Kirim jenis dan keterangan ke service
        Long id = izinSakitService.ajukanIzinSakit(username, tanggal, tingkat, jenis, keterangan, buktiBytes);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Daftar izin saya", description = "Menampilkan seluruh pengajuan izin milik mahasiswa login")
    @GetMapping
    public ResponseEntity<?> listPengajuanSaya() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<IzinSakitDetailResponse> list = izinSakitService.listPengajuanByUsername(username)
                .stream()
                .map(izin -> IzinSakitDetailResponse.builder()
                        .id(izin.getId())
                        .scheduleId(izin.getApelSchedule().getId())
                        .tanggal(izin.getApelSchedule().getTanggalApel())
                        .tingkat(izin.getApelSchedule().getTingkat())
                        .jenis(izin.getJenis())
                        .statusBukti(izin.getStatusBukti())
                        .keterangan(izin.getKeterangan())
                        .catatanAdmin(izin.getCatatanAdmin())
                        .buktiBase64(izin.getBukti() != null ? java.util.Base64.getEncoder().encodeToString(izin.getBukti()) : null)
                        .build())
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Riwayat izin saya", description = "Alias dari daftar izin saya")
    @GetMapping("/me")
    public ResponseEntity<?> listRiwayatIzinSaya() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<IzinSakitDetailResponse> list = izinSakitService.listPengajuanByUsername(username)
                .stream()
                .map(izin -> IzinSakitDetailResponse.builder()
                        .id(izin.getId())
                        .scheduleId(izin.getApelSchedule().getId())
                        .tanggal(izin.getApelSchedule().getTanggalApel())
                        .tingkat(izin.getApelSchedule().getTingkat())
                        .jenis(izin.getJenis())
                        .statusBukti(izin.getStatusBukti())
                        .keterangan(izin.getKeterangan())
                        .catatanAdmin(izin.getCatatanAdmin())
                        .buktiBase64(izin.getBukti() != null ? java.util.Base64.getEncoder().encodeToString(izin.getBukti()) : null)
                        .build())
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Detail izin", description = "Detail izin termasuk bukti (base64) dan status")
    @GetMapping("/{id}")
    public ResponseEntity<IzinSakitDetailResponse> detailIzin(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var izin = izinSakitService.getByIdAndUsername(id, username);
        String b64 = izin.getBukti() != null ? java.util.Base64.getEncoder().encodeToString(izin.getBukti()) : null;
        return ResponseEntity.ok(IzinSakitDetailResponse.builder()
                .id(izin.getId())
                .scheduleId(izin.getApelSchedule().getId())
                .tanggal(izin.getApelSchedule().getTanggalApel())
                .tingkat(izin.getApelSchedule().getTingkat())
                .jenis(izin.getJenis())
                .statusBukti(izin.getStatusBukti())
                .keterangan(izin.getKeterangan())
                .catatanAdmin(izin.getCatatanAdmin())
                .buktiBase64(b64)
                .build());
    }

    @Operation(summary = "Batalkan izin (pending)", description = "Mahasiswa dapat membatalkan jika status masih MENUNGGU")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIzinJikaPending(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        izinSakitService.deleteIfPendingByOwner(id, username);
        return ResponseEntity.noContent().build();
    }
}
