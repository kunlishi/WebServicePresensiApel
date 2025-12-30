package com.polstat.WebServiceApel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PresensiDetailResponse {
    private String nim;
    private String nama;
    private String kelas;
    private String status; // HADIR, TERLAMBAT, IZIN, SAKIT, TIDAK_HADIR
    private String catatan; // Jika ada catatan admin dari izin
}
