package com.proyecto.barberia.service;

import com.proyecto.barberia.model.Servicio;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IServicioService {
    List<Servicio> listarTodos();
    List<Servicio> listarActivos();
    Optional<Servicio> buscarPorId(Integer id);
    Servicio guardar(Servicio servicio, MultipartFile imagen) throws IOException;
    void eliminar(Integer id);
}