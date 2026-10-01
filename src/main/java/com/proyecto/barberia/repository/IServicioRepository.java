package com.proyecto.barberia.repository;

import com.proyecto.barberia.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IServicioRepository extends JpaRepository<Servicio, Integer> {
    List<Servicio> findByEstado(String estado);
}