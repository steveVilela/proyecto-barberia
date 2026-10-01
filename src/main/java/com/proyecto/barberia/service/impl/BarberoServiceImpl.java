package com.proyecto.barberia.service.impl;

import com.proyecto.barberia.model.Barbero;
import com.proyecto.barberia.repository.IBarberoRepository;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BarberoServiceImpl implements IBarberoService {

    @Autowired
    private IBarberoRepository barberoRepository;

    @Autowired
    private ICloudStorageService cloudStorageService;

    @Override
    public List<Barbero> listarTodos() {
        return barberoRepository.findAll();
    }

    @Override
    public List<Barbero> listarActivos() {
        return barberoRepository.findByEstado("ACTIVO");
    }

    @Override
    public Optional<Barbero> buscarPorId(Integer id) {
        return barberoRepository.findById(id);
    }

    @Override
    public Barbero guardar(Barbero barbero, MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            // Si ya tenía una foto previa, se elimina del bucket
            if (barbero.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(barbero.getFotoUrl());
            }
            // Subir la nueva foto a la carpeta "barberos" en el bucket
            String url = cloudStorageService.subirArchivo(imagen, "barberos");
            barbero.setFotoUrl(url);
        }
        if (barbero.getEstado() == null) {
            barbero.setEstado("ACTIVO");
        }
        return barberoRepository.save(barbero);
    }

    @Override
    public void eliminar(Integer id) {
        barberoRepository.findById(id).ifPresent(b -> {
            if (b.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(b.getFotoUrl());
            }
            barberoRepository.deleteById(id);
        });
    }
}