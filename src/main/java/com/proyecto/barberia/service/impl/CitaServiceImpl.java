package com.proyecto.barberia.service.impl;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.repository.ICitaRepository;
import com.proyecto.barberia.service.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements ICitaService {

    @Autowired
    private ICitaRepository citaRepository;

    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    @Override
    public List<Cita> listarUltimasCinco() {
        return citaRepository.findTop5ByOrderByIdCitaDesc();
    }

    @Override
    public Optional<Cita> buscarPorId(Integer id) {
        return citaRepository.findById(id);
    }

    @Override
    public Cita registrarOActualizar(Cita cita) {
        if (cita.getEstado() == null) {
            cita.setEstado("PENDIENTE");
        }
        return citaRepository.save(cita);
    }

    @Override
    public void cambiarEstado(Integer idCita, String nuevoEstado) {
        citaRepository.findById(idCita).ifPresent(cita -> {
            cita.setEstado(nuevoEstado);
            citaRepository.save(cita);
        });
    }
}