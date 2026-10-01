package com.proyecto.barberia.service;

import com.proyecto.barberia.model.Barbero;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IBarberoService {
    List<Barbero> listarTodos();
    List<Barbero> listarActivos();
    Optional<Barbero> buscarPorId(Integer id);
    Barbero guardar(Barbero barbero, MultipartFile imagen) throws IOException;
    void eliminar(Integer id);
}