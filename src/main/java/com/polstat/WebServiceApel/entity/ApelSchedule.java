package com.polstat.WebServiceApel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "apel_schedule")
public class ApelSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tanggalApel;

    @Column(nullable = false)
    private LocalTime waktuApel;

    @Column(nullable = false)
    private String tingkat; //misalnya: "1", "2", "3", dan "4"

    @Column
    private String keterangan; //opsional
}
