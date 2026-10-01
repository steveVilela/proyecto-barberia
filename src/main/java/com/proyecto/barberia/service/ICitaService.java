package com.proyecto.barberia.service;

import com.proyecto.barberia.model.Cita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICitaService {
    List<Cita> listarTodas();
    List<Cita> listarPorFecha(LocalDate fecha);
    List<Cita> listarUltimasCinco();
    Optional<Cita> buscarPorId(Integer id);
    Cita registrarOActualizar(Cita cita);
    void cambiarEstado(Integer idCita, String nuevoEstado);
}