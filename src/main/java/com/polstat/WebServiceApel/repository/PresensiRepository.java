package com.polstat.WebServiceApel.repository;

import com.polstat.WebServiceApel.entity.Presensi;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PresensiRepository extends JpaRepository<Presensi,Long> {
    boolean existsByMahasiswaAndApelSchedule(Mahasiswa mahasiswa, ApelSchedule apelSchedule);
    List<Presensi> findByMahasiswaAndApelSchedule(Mahasiswa mahasiswa, ApelSchedule apelSchedule);

    @Query("select p from Presensi p join fetch p.mahasiswa m join fetch p.apelSchedule s where s.id = :scheduleId")
    List<Presensi> findAllWithMahasiswaBySchedule(@Param("scheduleId") Long scheduleId);

    List<Presensi> findByApelSchedule(ApelSchedule apelSchedule);

    List<Presensi> findByMahasiswaOrderByWaktuPresensiDesc(Mahasiswa mahasiswa);

    void deleteByMahasiswa(Mahasiswa mahasiswa);
}
