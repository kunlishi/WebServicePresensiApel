package com.polstat.WebServiceApel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "presensi")
public class Presensi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private Mahasiswa mahasiswa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="apel_schedule_id", nullable = false)
    private ApelSchedule apelSchedule;

    @Column(nullable = false)
    private LocalDateTime waktuPresensi;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; //HADIR, TERLAMBAT, TIDAK_HADIR

    @Column
    private String createdBySpd; // username SPD yang mencatat

    public enum Status {
        HADIR, TERLAMBAT, TIDAK_HADIR
    }
}
