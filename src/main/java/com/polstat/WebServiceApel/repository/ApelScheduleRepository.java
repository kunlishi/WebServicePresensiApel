package com.polstat.WebServiceApel.repository;

import com.polstat.WebServiceApel.entity.ApelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApelScheduleRepository extends JpaRepository<ApelSchedule,Long> {
    Optional<ApelSchedule> findByTanggalApelAndTingkat(LocalDate tanggalApel, String tingkat);
    List<ApelSchedule> findByTanggalApel(LocalDate tanggalApel);
}
