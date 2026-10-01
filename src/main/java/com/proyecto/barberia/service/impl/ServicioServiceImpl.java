package com.proyecto.barberia.service.impl;

import com.proyecto.barberia.model.Servicio;
import com.proyecto.barberia.repository.IServicioRepository;
import com.proyecto.barberia.service.ICloudStorageService;
import com.proyecto.barberia.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioServiceImpl implements IServicioService {

    @Autowired
    private IServicioRepository servicioRepository;

    @Autowired
    private ICloudStorageService cloudStorageService;

    @Override
    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }

    @Override
    public List<Servicio> listarActivos() {
        return servicioRepository.findByEstado("ACTIVO");
    }

    @Override
    public Optional<Servicio> buscarPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    @Override
    public Servicio guardar(Servicio servicio, MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            if (servicio.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(servicio.getFotoUrl());
            }
            String url = cloudStorageService.subirArchivo(imagen, "servicios");
            servicio.setFotoUrl(url);
        }
        if (servicio.getEstado() == null) {
            servicio.setEstado("ACTIVO");
        }
        return servicioRepository.save(servicio);
    }

    @Override
    public void eliminar(Integer id) {
        servicioRepository.findById(id).ifPresent(s -> {
            if (s.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(s.getFotoUrl());
            }
            servicioRepository.deleteById(id);
        });
    }
}