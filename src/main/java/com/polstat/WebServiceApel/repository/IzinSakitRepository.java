package com.polstat.WebServiceApel.repository;

import com.polstat.WebServiceApel.entity.IzinSakit;
import com.polstat.WebServiceApel.entity.Mahasiswa;
import com.polstat.WebServiceApel.entity.ApelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface IzinSakitRepository extends JpaRepository<IzinSakit,Long> {
    List<IzinSakit> findByMahasiswaAndApelSchedule(Mahasiswa mahasiswa, ApelSchedule apelSchedule);

    @Query("select i from IzinSakit i join fetch i.mahasiswa m join fetch i.apelSchedule s where s.id = :scheduleId")
    List<IzinSakit> findAllWithMahasiswaBySchedule(@Param("scheduleId") Long scheduleId);
    List<IzinSakit> findByApelScheduleAndStatusBukti(ApelSchedule schedule, IzinSakit.Status status);

    void deleteByMahasiswa(Mahasiswa mahasiswa);
}
