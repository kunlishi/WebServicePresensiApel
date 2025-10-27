package com.polstat.WebServiceApel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFirstRegisterRequest {
    private String username;
    private String password;
    private String name; // opsional display name admin
}
