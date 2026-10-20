package com.proyecto.barberia.repository;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFecha(LocalDate fecha);
    List<Cita> findTop5ByOrderByIdCitaDesc();
    
    // Buscar citas solo del cliente ordenadas por fecha/hora
    List<Cita> findByUsuarioOrderByFechaDescHoraDesc(Usuario usuario);

    // NUEVO: Validar si un barbero ya tiene una cita activa en esa fecha y hora
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.barbero.idBarbero = :idBarbero AND c.fecha = :fecha AND c.hora = :hora AND c.estado != 'CANCELADA'")
    boolean existeCitaActiva(
            @Param("idBarbero") Integer idBarbero, 
            @Param("fecha") LocalDate fecha, 
            @Param("hora") LocalTime hora
    );
}
