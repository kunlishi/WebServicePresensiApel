package com.polstat.WebServiceApel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "izin_sakit")
public class IzinSakit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private Mahasiswa mahasiswa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="apel_schedul_id", nullable = false)
    private ApelSchedule apelSchedule;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Jenis jenis; //IZIN atau SAKIT

    @Lob
    private byte[] bukti; //file dikirim langsung

    @Column
    private String keterangan;

    @Column
    private String catatanAdmin;

    @Enumerated(EnumType.STRING)
    private Status statusBukti; //DITERIMA, DITOLAK, MENUNGGU

    public enum Jenis {IZIN, SAKIT}

    public enum Status {DITERIMA, DITOLAK, MENUNGGU}
}
