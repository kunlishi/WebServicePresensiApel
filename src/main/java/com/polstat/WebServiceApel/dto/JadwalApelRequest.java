package com.polstat.WebServiceApel.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class JadwalApelRequest {
    private LocalDate tanggal;
    private LocalTime waktu; // HH:mm:ss
    private String tingkat; // 1,2,3,4
    private String keterangan; // optional
}
