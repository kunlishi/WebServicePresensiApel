package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.JadwalApelRequest;
import com.polstat.WebServiceApel.dto.PresensiDetailResponse;
import com.polstat.WebServiceApel.dto.PresensiRecordResponse;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.IzinSakit;
import com.polstat.WebServiceApel.dto.AdminIzinSakitSummary;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.entity.Presensi;
import com.polstat.WebServiceApel.repository.ApelScheduleRepository;
import com.polstat.WebServiceApel.repository.IzinSakitRepository;
import com.polstat.WebServiceApel.repository.MahasiswaRepository;
import com.polstat.WebServiceApel.repository.PresensiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ApelScheduleRepository apelScheduleRepository;
    private final IzinSakitRepository izinSakitRepository;
    private final PresensiRepository presensiRepository;
    private final MahasiswaRepository mahasiswaRepository;

    public Long createJadwal(JadwalApelRequest request) {
        ApelSchedule schedule = apelScheduleRepository.save(
                ApelSchedule.builder()
                        .tanggalApel(request.getTanggal())
                        .waktuApel(request.getWaktu()) // Jam Mulai
                        .tingkat(request.getTingkat())
                        .keterangan(request.getKeterangan()) // Contoh: "Apel Pagi rutin" atau "Apel Pengarahan"
                        .build()
        );
        return schedule.getId();
    }

    public List<PresensiRecordResponse> getFullRekapByJadwal(Long scheduleId) {
        ApelSchedule schedule = apelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Jadwal tidak ditemukan"));
        List<Mahasiswa> allMahasiswa = mahasiswaRepository.findByTingkat(schedule.getTingkat());
        List<Presensi> existingPresensi = presensiRepository.findByApelSchedule(schedule);
        Map<String, Presensi> presensiMap = existingPresensi.stream()
                .collect(Collectors.toMap(p -> p.getMahasiswa().getNim(), p -> p));

        return allMahasiswa.stream().map(mhs -> {
            Presensi p = presensiMap.get(mhs.getNim());

            return PresensiRecordResponse.builder()
                    .scheduleId(schedule.getId())
                    .tanggal(schedule.getTanggalApel())
                    .tingkat(schedule.getTingkat())
                    .nim(mhs.getNim())
                    .nama(mhs.getNama())
                    .kelas(mhs.getKelas())
                    .waktuPresensi(p != null ? p.getWaktuPresensi() : null)
                    .status(p != null ? p.getStatus() : Presensi.Status.TIDAK_HADIR)
                    .createdBySpd(p != null ? p.getCreatedBySpd() : "-")
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PresensiDetailResponse> getRekapPresensi(Long scheduleId) {
        // 1. Ambil info jadwal
        ApelSchedule schedule = apelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Jadwal tidak ditemukan"));

        // 2. Ambil semua mahasiswa di tingkat yang sama
        List<Mahasiswa> daftarMahasiswa = mahasiswaRepository.findByTingkat(schedule.getTingkat());

        // 3. Ambil semua data presensi & izin yang sudah masuk untuk jadwal ini
        List<Presensi> listPresensi = presensiRepository.findByApelSchedule(schedule);
        List<IzinSakit> listIzin = izinSakitRepository.findByApelScheduleAndStatusBukti(schedule, IzinSakit.Status.DITERIMA);

        // 4. Gabungkan datanya
        return daftarMahasiswa.stream().map(mhs -> {
            // Cek apakah ada di tabel presensi
            Optional<Presensi> p = listPresensi.stream()
                    .filter(pres -> pres.getMahasiswa().getNim().equals(mhs.getNim()))
                    .findFirst();

            // Cek apakah ada di tabel izin (yang sudah disetujui)
            Optional<IzinSakit> iz = listIzin.stream()
                    .filter(i -> i.getMahasiswa().getNim().equals(mhs.getNim()))
                    .findFirst();

            String finalStatus = "TIDAK_HADIR"; // Default jika tidak ada data sama sekali
            String catatan = "";

            if (p.isPresent()) {
                finalStatus = p.get().getStatus().toString(); // HADIR atau TERLAMBAT
            } else if (iz.isPresent()) {
                finalStatus = iz.get().getJenis().toString(); // IZIN atau SAKIT
                catatan = iz.get().getCatatanAdmin();
            }

            return PresensiDetailResponse.builder()
                    .nim(mhs.getNim())
                    .nama(mhs.getNama())
                    .kelas(mhs.getKelas())
                    .status(finalStatus)
                    .catatan(catatan)
                    .build();
        }).collect(Collectors.toList());
    }

    public void validateIzin(Long id, IzinSakit.Status status, String catatan) {
        IzinSakit izin = izinSakitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Izin tidak ditemukan"));
        izin.setStatusBukti(status);
        izin.setCatatanAdmin(catatan); // Simpan catatan admin ke database
        izinSakitRepository.save(izin);
    }

    public long clearAllBukti() {
        List<IzinSakit> all = izinSakitRepository.findAll();
        long affected = 0;
        for (IzinSakit i : all) {
            if (i.getBukti() != null && i.getBukti().length > 0) {
                i.setBukti(null);
                izinSakitRepository.save(i);
                affected++;
            }
        }
        return affected;
    }

    public List<ApelSchedule> listJadwal() {
        return apelScheduleRepository.findAll();
    }

    public void updateJadwal(Long id, JadwalApelRequest request) {
        ApelSchedule s = apelScheduleRepository.findById(id).orElseThrow();
        if (request.getTanggal() != null) s.setTanggalApel(request.getTanggal());
        if (request.getWaktu() != null) s.setWaktuApel(request.getWaktu());
        if (request.getTingkat() != null && !request.getTingkat().isBlank()) s.setTingkat(request.getTingkat());
        if (request.getKeterangan() != null) s.setKeterangan(request.getKeterangan());
        apelScheduleRepository.save(s);
    }

    public void deleteJadwal(Long id) {
        if (!apelScheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Jadwal tidak ditemukan");
        }
        apelScheduleRepository.deleteById(id);
    }

    public List<AdminIzinSakitSummary> listIzin() {
        return izinSakitRepository.findAll().stream().map(izin -> AdminIzinSakitSummary.builder()
                .id(izin.getId())
                .scheduleId(izin.getApelSchedule().getId())
                .tanggal(izin.getApelSchedule().getTanggalApel())
                .tingkat(izin.getApelSchedule().getTingkat())
                .mahasiswaNim(izin.getMahasiswa().getNim())
                .mahasiswaNama(izin.getMahasiswa().getNama())
                .jenis(izin.getJenis())
                .statusBukti(izin.getStatusBukti())
                .keterangan(izin.getKeterangan())
                .catatanAdmin(izin.getCatatanAdmin())
                .hasBukti(izin.getBukti() != null && izin.getBukti().length > 0)
                .buktiUrl(null)
                .buktiBase64(izin.getBukti() != null && izin.getBukti().length > 0
                        ? Base64.getEncoder().encodeToString(izin.getBukti())
                        : null)
                .build()).toList();
    }

    public List<com.polstat.WebServiceApel.dto.PresensiRecordResponse> listPresensiAll() {
        // Hindari serialisasi entity LAZY ke JSON: map ke DTO ringkas
        java.util.List<com.polstat.WebServiceApel.dto.PresensiRecordResponse> result = new java.util.ArrayList<>();
        for (com.polstat.WebServiceApel.entity.Presensi p : presensiRepository.findAll()) {
            com.polstat.WebServiceApel.entity.ApelSchedule s = p.getApelSchedule();
            com.polstat.WebServiceApel.entity.Mahasiswa m = p.getMahasiswa();
            result.add(com.polstat.WebServiceApel.dto.PresensiRecordResponse.builder()
                    .scheduleId(s.getId())
                    .tanggal(s.getTanggalApel())
                    .tingkat(s.getTingkat())
                    .nim(m.getNim())
                    .nama(m.getNama())
                    .waktuPresensi(p.getWaktuPresensi())
                    .status(p.getStatus())
                    .createdBySpd(p.getCreatedBySpd())
                    .build());
        }
        return result;
    }

}
