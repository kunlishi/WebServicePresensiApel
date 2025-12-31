package com.polstat.WebServiceApel.dto;

import com.polstat.WebServiceApel.entity.Presensi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresensiRecordResponse {
    private Long id;
    private Long scheduleId;
    private LocalDate tanggal;
    private String tingkat;
    private String nim;
    private String nama;
    private String kelas;
    private LocalDateTime waktuPresensi;
    private Presensi.Status status;
    private String createdBySpd;
}

