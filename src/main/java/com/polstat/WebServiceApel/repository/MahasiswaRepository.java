package com.polstat.WebServiceApel.repository;

import com.polstat.WebServiceApel.entity.Mahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MahasiswaRepository extends JpaRepository<Mahasiswa, Long> {
    Optional<Mahasiswa> findByNim(String nim);
    Optional<Mahasiswa> findByUser_Username(String username);
    java.util.List<Mahasiswa> findByTingkat(String tingkat);
}
