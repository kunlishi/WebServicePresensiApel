package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.PresensiBatchRequest;
import com.polstat.WebServiceApel.dto.PresensiBatchResponse;
import com.polstat.WebServiceApel.dto.PresensiRecordResponse;
import com.polstat.WebServiceApel.dto.PresensiRequest;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.entity.Presensi;
import com.polstat.WebServiceApel.repository.ApelScheduleRepository;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import com.polstat.WebServiceApel.repository.PresensiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresensiService {
    private final PresensiRepository presensiRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final ApelScheduleRepository apelScheduleRepository;

    public PresensiBatchResponse saveBatch(PresensiBatchRequest request) {
        ApelSchedule schedule = apelScheduleRepository
                .findByTanggalApelAndTingkat(request.getTanggal(), request.getTingkat())
                .orElseThrow(() -> new IllegalArgumentException("Jadwal apel tidak ditemukan untuk tanggal/tingkat tersebut"));

        long saved = 0;
        long ignored = 0;

        for (String nim : request.getMahasiswa()) {
            Mahasiswa mhs = mahasiswaRepository.findByNim(nim).orElse(null);
            if (mhs == null) {
                ignored++;
                continue;
            }
            List<Presensi> existing = presensiRepository.findByMahasiswaAndApelSchedule(mhs, schedule);
            if (!existing.isEmpty()) {
                ignored++;
                continue;
            }
            Presensi presensi = Presensi.builder()
                    .mahasiswa(mhs)
                    .apelSchedule(schedule)
                    .waktuPresensi(LocalDateTime.now())
                    .status(Presensi.Status.HADIR)
                    .createdBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())
                    .build();
            presensiRepository.save(presensi);
            saved++;
        }

        return PresensiBatchResponse.builder()
                .savedCount(saved)
                .ignoredCount(ignored)
                .scheduleId(schedule.getId())
                .build();
    }

    public List<com.polstat.WebServiceApel.dto.PresensiRecordResponse> listPresensi(java.time.LocalDate tanggal, String tingkat) {
        List<ApelSchedule> schedules;
        if (tanggal != null && tingkat != null) {
            schedules = apelScheduleRepository.findByTanggalApelAndTingkat(tanggal, tingkat)
                    .map(java.util.List::of)
                    .orElse(java.util.List.of());
        } else if (tanggal != null) {
            schedules = apelScheduleRepository.findByTanggalApel(tanggal);
        } else {
            schedules = apelScheduleRepository.findAll();
        }
        java.util.List<com.polstat.WebServiceApel.dto.PresensiRecordResponse> result = new java.util.ArrayList<>();
        for (ApelSchedule s : schedules) {
            List<Presensi> list = presensiRepository.findAllWithMahasiswaBySchedule(s.getId());
            for (Presensi p : list) {
                result.add(com.polstat.WebServiceApel.dto.PresensiRecordResponse.builder()
                        .scheduleId(s.getId())
                        .tanggal(s.getTanggalApel())
                        .tingkat(s.getTingkat())
                        .nim(p.getMahasiswa().getNim())
                        .nama(p.getMahasiswa().getNama())
                        .waktuPresensi(p.getWaktuPresensi())
                        .status(p.getStatus())
                        .createdBySpd(p.getCreatedBySpd())
                        .build());
            }
        }
        return result;
    }

    public void deletePresensiById(Long id) {
        presensiRepository.deleteById(id);
    }

    public long markTerlambat(java.time.LocalDate tanggal, String tingkat, java.util.List<String> nims) {
        ApelSchedule schedule = apelScheduleRepository
                .findByTanggalApelAndTingkat(tanggal, tingkat)
                .orElseThrow(() -> new IllegalArgumentException("Jadwal apel tidak ditemukan untuk tanggal/tingkat tersebut"));

        long updated = 0;
        for (String nim : nims) {
            Mahasiswa mhs = mahasiswaRepository.findByNim(nim).orElse(null);
            if (mhs == null) continue;
            List<Presensi> existing = presensiRepository.findByMahasiswaAndApelSchedule(mhs, schedule);
            if (existing.isEmpty()) {
                // belum ada presensi, buat langsung sebagai terlambat
                Presensi p = Presensi.builder()
                        .mahasiswa(mhs)
                        .apelSchedule(schedule)
                        .waktuPresensi(java.time.LocalDateTime.now())
                        .status(Presensi.Status.TERLAMBAT)
                        .createdBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())
                        .build();
                presensiRepository.save(p);
                updated++;
            } else {
                // ubah status menjadi terlambat jika belum terlambat
                Presensi p = existing.get(0);
                if (p.getStatus() != Presensi.Status.TERLAMBAT) {
                    p.setStatus(Presensi.Status.TERLAMBAT);
                    p.setCreatedBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
                    presensiRepository.save(p);
                    updated++;
                }
            }
        }
        return updated;
    }
    public PresensiRecordResponse savePresensiSingle(com.polstat.WebServiceApel.dto.PresensiRequest request) {
        // 1. Cari Jadwal (Menggunakan query yang sama dengan saveBatch)
        ApelSchedule schedule = apelScheduleRepository
                .findByTanggalApelAndTingkat(java.time.LocalDate.parse(request.getTanggal()), request.getTingkat())
                .orElseThrow(() -> new IllegalArgumentException("Jadwal apel tidak ditemukan"));

        // 2. Cari Mahasiswa
        Mahasiswa mhs = mahasiswaRepository.findByNim(request.getNim())
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa dengan NIM " + request.getNim() + " tidak ditemukan"));

        // 3. Cek Duplikasi (Sesuai logika saveBatch)
        if (!presensiRepository.findByMahasiswaAndApelSchedule(mhs, schedule).isEmpty()) {
            throw new IllegalArgumentException("Mahasiswa sudah melakukan presensi pada jadwal ini");
        }

        // 4. Simpan Data
        Presensi presensi = Presensi.builder()
                .mahasiswa(mhs)
                .apelSchedule(schedule)
                .waktuPresensi(LocalDateTime.now())
                .status(Presensi.Status.HADIR)
                .createdBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())
                .build();

        Presensi saved = presensiRepository.save(presensi);

        // 5. Kembalikan Response (Seragam dengan listPresensi)
        return mapToRecordResponse(saved, schedule);
    }

    public PresensiRecordResponse markTerlambatSingle(com.polstat.WebServiceApel.dto.PresensiRequest request) {
        ApelSchedule schedule = apelScheduleRepository
                .findByTanggalApelAndTingkat(java.time.LocalDate.parse(request.getTanggal()), request.getTingkat())
                .orElseThrow(() -> new IllegalArgumentException("Jadwal apel tidak ditemukan"));

        Mahasiswa mhs = mahasiswaRepository.findByNim(request.getNim())
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa tidak ditemukan"));

        List<Presensi> existing = presensiRepository.findByMahasiswaAndApelSchedule(mhs, schedule);
        Presensi p;

        if (existing.isEmpty()) {
            p = Presensi.builder()
                    .mahasiswa(mhs)
                    .apelSchedule(schedule)
                    .waktuPresensi(LocalDateTime.now())
                    .status(Presensi.Status.TERLAMBAT)
                    .createdBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName())
                    .build();
        } else {
            p = existing.get(0);
            p.setStatus(Presensi.Status.TERLAMBAT);
            p.setCreatedBySpd(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        }

        Presensi saved = presensiRepository.save(p);
        return mapToRecordResponse(saved, schedule);
    }

    // Helper method agar kode bersih dan seragam
    private PresensiRecordResponse mapToRecordResponse(Presensi p, ApelSchedule s) {
        return PresensiRecordResponse.builder()
                .scheduleId(s.getId())
                .tanggal(s.getTanggalApel())
                .tingkat(s.getTingkat())
                .nim(p.getMahasiswa().getNim())
                .nama(p.getMahasiswa().getNama())
                .waktuPresensi(p.getWaktuPresensi())
                .status(p.getStatus())
                .createdBySpd(p.getCreatedBySpd())
                .build();
    }

    public List<PresensiRecordResponse> getRiwayatByNim(String nim) {
        Mahasiswa mhs = mahasiswaRepository.findByNim(nim)
                .orElseThrow(() -> new IllegalArgumentException("Mahasiswa tidak ditemukan"));

        List<Presensi> riwayat = presensiRepository.findByMahasiswaOrderByWaktuPresensiDesc(mhs);

        return riwayat.stream()
                .map(p -> PresensiRecordResponse.builder()
                        .scheduleId(p.getApelSchedule().getId())
                        .tanggal(p.getApelSchedule().getTanggalApel())
                        .tingkat(p.getApelSchedule().getTingkat())
                        .nim(p.getMahasiswa().getNim())
                        .nama(p.getMahasiswa().getNama())
                        .waktuPresensi(p.getWaktuPresensi())
                        .status(p.getStatus())
                        .createdBySpd(p.getCreatedBySpd())
                        .build())
                .collect(Collectors.toList());
    }
}
