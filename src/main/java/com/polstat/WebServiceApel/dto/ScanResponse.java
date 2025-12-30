package com.polstat.WebServiceApel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanResponse {
    private String status; // HADIR, TERLAMBAT, NEED_CONFIRMATION, ALREADY_PRESENT, ERROR
    private String message;
    private String nim;
    private String nama;
}
