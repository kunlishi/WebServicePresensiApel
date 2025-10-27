package com.polstat.WebServiceApel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table (name = "mahasiswa")
public class Mahasiswa {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    private String nim;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false)
    private String kelas;

    @Column
    private String tingkat; // 1,2,3,4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
