package com.polstat.WebServiceApel.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IzinSakitRequest {
    private LocalDate tanggal; // tanggal apel
    private String tingkat;    // tingkat apel
    private String alasan;     // alasan bebas
    private String bukti;      // base64 encoded file
}
