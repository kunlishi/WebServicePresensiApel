package com.polstat.WebServiceApel.dto;

import com.polstat.WebServiceApel.entity.IzinSakit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminIzinSakitSummary {
    private Long id;
    private Long scheduleId;
    private LocalDate tanggal;
    private String tingkat;
    private String mahasiswaNim;
    private String mahasiswaNama;
    private IzinSakit.Jenis jenis;
    private IzinSakit.Status statusBukti;
    private String keterangan;
    private String catatanAdmin;
    private boolean hasBukti;
    private String buktiUrl;     // link untuk unduh bukti
    private String buktiBase64;  // opsional, hanya diisi jika diminta
}
