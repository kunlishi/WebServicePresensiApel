package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.MahasiswaResponse;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MahasiswaService {
    private final MahasiswaRepository mahasiswaRepository;

    public MahasiswaResponse getProfileByUsername(String username) {
        Mahasiswa mhs = mahasiswaRepository.findByUser_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa tidak ditemukan"));
        return MahasiswaResponse.builder()
                .nim(mhs.getNim())
                .nama(mhs.getNama())
                .kelas(mhs.getKelas())
                .tingkat(mhs.getTingkat())
                .build();
    }

    public List<MahasiswaResponse> findAll() {
        return mahasiswaRepository.findAll().stream()
                .map(m -> MahasiswaResponse.builder()
                        .nim(m.getNim())
                        .nama(m.getNama())
                        .kelas(m.getKelas())
                        .tingkat(m.getTingkat())
                        .build())
                .toList();
    }

    public MahasiswaResponse findByNim(String nim) {
        Mahasiswa m = mahasiswaRepository.findByNim(nim)
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa tidak ditemukan"));
        return MahasiswaResponse.builder()
                .nim(m.getNim())
                .nama(m.getNama())
                .kelas(m.getKelas())
                .tingkat(m.getTingkat())
                .build();
    }

    public List<MahasiswaResponse> findByTingkat(String tingkat) {
        return mahasiswaRepository.findByTingkat(tingkat).stream()
                .map(m -> MahasiswaResponse.builder()
                        .nim(m.getNim())
                        .nama(m.getNama())
                        .kelas(m.getKelas())
                        .tingkat(m.getTingkat())
                        .build())
                .toList();
    }
}
