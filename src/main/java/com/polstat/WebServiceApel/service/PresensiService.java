package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.*;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.entity.Presensi;
import com.polstat.WebServiceApel.repository.ApelScheduleRepository;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import com.polstat.WebServiceApel.repository.PresensiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresensiService {
    private final PresensiRepository presensiRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final ApelScheduleRepository apelScheduleRepository;

    @Transactional
    public ScanResponse processScan(String nim, Long scheduleId) {
        Mahasiswa mahasiswa = mahasiswaRepository.findByNim(nim);
        if (mahasiswa == null) {
            return new ScanResponse("ERROR", "Mahasiswa tidak ditemukan", null, null);
        }

        ApelSchedule schedule = apelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Jadwal tidak ditemukan"));

        // 1. Cek apakah sudah absen
        if (presensiRepository.existsByMahasiswaAndApelSchedule(mahasiswa, schedule)) {
            return new ScanResponse("ALREADY_PRESENT", "Mahasiswa sudah absen", nim, mahasiswa.getNama());
        }

        LocalTime now = LocalTime.now();
        LocalTime startTime = schedule.getJamMulai();

        // Batas Toleransi: Jadwal - 5 menit s/d Jadwal + 5 menit
        LocalTime lowerBound = startTime.minusMinutes(5);
        LocalTime upperBound = startTime.plusMinutes(5);

        // 2. Logika Penentuan Status
        if (now.isAfter(lowerBound) && now.isBefore(upperBound)) {
            // RENTANG KRITIS: Jangan simpan ke DB dulu, minta konfirmasi petugas
            return ScanResponse.builder()
                    .status("NEED_CONFIRMATION")
                    .message("Waktu transisi (±5 mnt). Pilih status manual:")
                    .nim(nim)
                    .nama(mahasiswa.getNama())
                    .build();
        } else if (now.isBefore(lowerBound)) {
            // Masih pagi (lebih dari 5 menit sebelum mulai) -> HADIR
            saveToDb(mahasiswa, schedule, now, Presensi.Status.HADIR);
            return new ScanResponse("HADIR", "Presensi Berhasil (Tepat Waktu)", nim, mahasiswa.getNama());
        } else {
            // Sudah lewat (lebih dari 5 menit setelah mulai) -> TERLAMBAT
            saveToDb(mahasiswa, schedule, now, Presensi.Status.TERLAMBAT);
            return new ScanResponse("TERLAMBAT", "Presensi Berhasil (Terlambat)", nim, mahasiswa.getNama());
        }
    }

    @Transactional
    public void confirmManual(String nim, Long scheduleId, String status) {
        Mahasiswa mahasiswa = mahasiswaRepository.findByNim(nim);
        ApelSchedule schedule = apelScheduleRepository.findById(scheduleId).get();

        saveToDb(mahasiswa, schedule, LocalTime.now(), Presensi.Status.valueOf(status.toUpperCase()));
    }

    private void saveToDb(Mahasiswa m, ApelSchedule s, LocalTime t, Presensi.Status st) {
        Presensi presensi = Presensi.builder()
                .mahasiswa(m)
                .apelSchedule(s)
                .waktuScan(t)
                .status(st)
                .build();
        presensiRepository.save(presensi);
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
