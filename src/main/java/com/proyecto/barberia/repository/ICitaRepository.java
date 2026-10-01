package com.proyecto.barberia.repository;

import com.proyecto.barberia.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findTop5ByOrderByIdCitaDesc();
}