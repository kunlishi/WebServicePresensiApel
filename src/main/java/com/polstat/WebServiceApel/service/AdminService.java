package com.polstat.WebServiceApel.service;

import com.polstat.WebServiceApel.dto.JadwalApelRequest;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import com.polstat.WebServiceApel.entity.IzinSakit;
import com.polstat.WebServiceApel.dto.AdminIzinSakitSummary;
import com.polstat.WebServiceApel.repository.ApelScheduleRepository;
import com.polstat.WebServiceApel.repository.IzinSakitRepository;
import com.polstat.WebServiceApel.repository.PresensiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ApelScheduleRepository apelScheduleRepository;
    private final IzinSakitRepository izinSakitRepository;
    private final PresensiRepository presensiRepository;

    public Long createJadwal(JadwalApelRequest request) {
        ApelSchedule schedule = apelScheduleRepository.save(
                ApelSchedule.builder()
                        .tanggalApel(request.getTanggal())
                        .waktuApel(request.getWaktu())
                        .tingkat(request.getTingkat())
                        .keterangan(request.getKeterangan())
                        .build()
        );
        return schedule.getId();
    }

    public void validateIzin(Long id, IzinSakit.Status status) {
        IzinSakit izin = izinSakitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Izin/Sakit tidak ditemukan"));
        izin.setStatusBukti(status);
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
                .alasan(izin.getAlasan())
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
