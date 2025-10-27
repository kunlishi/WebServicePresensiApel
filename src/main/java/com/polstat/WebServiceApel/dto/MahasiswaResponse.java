package com.polstat.WebServiceApel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MahasiswaResponse {
    private String nim;
    private String nama;
    private String kelas;
    private String tingkat;
}
