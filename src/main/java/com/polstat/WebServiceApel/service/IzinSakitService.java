package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.IzinSakitRequest;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.IzinSakit;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.repository.ApelScheduleRepository;
import com.polstat.WebServiceApel.repository.UserRepository;
import com.polstat.WebServiceApel.entity.User;
import com.polstat.WebServiceApel.repository.IzinSakitRepository;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IzinSakitService {
    private final IzinSakitRepository izinSakitRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final ApelScheduleRepository apelScheduleRepository;
    private final UserRepository userRepository;

    public Long ajukanIzinSakit(String username,
                                 java.time.LocalDate tanggal,
                                 String tingkat,
                                 String jenis,
                                 String keterangan,
                                 byte[] buktiBytes) {
        // Pastikan entity Mahasiswa terhubung dengan user ini (link berdasarkan user.username)
        Mahasiswa mahasiswa = mahasiswaRepository.findByUser_Username(username)
                .orElseGet(() -> {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
                    // Coba cocokkan berdasarkan NIM == username
                    Mahasiswa byNim = mahasiswaRepository.findByNim(username).orElse(null);
                    if (byNim != null) {
                        if (byNim.getUser() == null || !byNim.getUser().getId().equals(user.getId())) {
                            byNim.setUser(user);
                            return mahasiswaRepository.save(byNim);
                        }
                        return byNim;
                    }
                    // Auto-provision Mahasiswa minimal yang tertaut ke user
                    Mahasiswa baru = Mahasiswa.builder()
                            .nim(username)
                            .nama(username)
                            .kelas("")
                            .tingkat(tingkat)
                            .user(user)
                            .build();
                    return mahasiswaRepository.save(baru);
                });

        String targetTingkat = (tingkat != null && !tingkat.isBlank()) ? tingkat : mahasiswa.getTingkat();

        ApelSchedule schedule = apelScheduleRepository
                .findByTanggalApelAndTingkat(tanggal, targetTingkat)
                .orElseThrow(() -> new IllegalArgumentException("Jadwal apel tidak ditemukan"));

        IzinSakit izin = IzinSakit.builder()
                .mahasiswa(mahasiswa)
                .apelSchedule(schedule)
                .jenis(IzinSakit.Jenis.SAKIT)
                .bukti(buktiBytes)
                .catatanAdmin(null)
                .keterangan(keterangan)
                .statusBukti(IzinSakit.Status.MENUNGGU)
                .build();

        return izinSakitRepository.save(izin).getId();
    }

    public List<IzinSakit> listPengajuanByUsername(String username) {
        Mahasiswa mahasiswa = mahasiswaRepository.findByUser_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa untuk user tidak ditemukan"));
        // ambil semua pengajuan mahasiswa terkait (bisa filter jika dibutuhkan)
        return izinSakitRepository.findAll().stream()
                .filter(i -> i.getMahasiswa().getId().equals(mahasiswa.getId()))
                .collect(Collectors.toList());
    }

    public IzinSakit getByIdAndUsername(Long id, String username) {
        Mahasiswa mahasiswa = mahasiswaRepository.findByUser_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa untuk user tidak ditemukan"));
        IzinSakit izin = izinSakitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Izin tidak ditemukan"));
        if (!izin.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new IllegalArgumentException("Tidak berhak mengakses izin ini");
        }
        return izin;
    }

    public void deleteIfPendingByOwner(Long id, String username) {
        IzinSakit izin = getByIdAndUsername(id, username);
        if (izin.getStatusBukti() == IzinSakit.Status.MENUNGGU) {
            izinSakitRepository.delete(izin);
        } else {
            throw new IllegalStateException("Izin tidak dalam status pending");
        }
    }
}
