package com.polstat.WebServiceApel.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TerlambatRequest {
    private LocalDate tanggal; // yyyy-MM-dd
    private String tingkat;    // 1,2,3,4
    private List<String> mahasiswa; // daftar NIM
}
