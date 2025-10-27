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
public class IzinSakitDetailResponse {
    private Long id;
    private Long scheduleId;
    private LocalDate tanggal;
    private String tingkat;
    private IzinSakit.Jenis jenis;
    private IzinSakit.Status statusBukti;
    private String alasan;
    private String catatanAdmin;
    private String buktiBase64;
}
