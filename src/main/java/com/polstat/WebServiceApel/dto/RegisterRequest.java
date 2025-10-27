package com.polstat.WebServiceApel.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    // khusus registrasi mahasiswa
    private String nama;
    private String kelas;
    private String tingkat; // 1|2|3|4
}
