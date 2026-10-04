package com.proyecto.barberia.repository;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findTop5ByOrderByIdCitaDesc();
    
    // NUEVO: Buscar citas solo del cliente ordenadas por fecha/hora
    List<Cita> findByUsuarioOrderByFechaDescHoraDesc(Usuario usuario);
}