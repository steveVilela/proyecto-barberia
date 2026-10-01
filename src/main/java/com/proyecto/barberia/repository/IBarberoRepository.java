package com.proyecto.barberia.repository;

import com.proyecto.barberia.model.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IBarberoRepository extends JpaRepository<Barbero, Integer> {
    List<Barbero> findByEstado(String estado);
}