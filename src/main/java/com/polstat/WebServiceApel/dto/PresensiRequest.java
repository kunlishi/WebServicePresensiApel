package com.polstat.WebServiceApel.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PresensiRequest {
    private String tanggal;
    private String nim;
    private String tingkat;
}
