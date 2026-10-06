package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Servicio;
import com.proyecto.barberia.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // Permite peticiones desde emuladores Android o clientes externos
@RestController
@RequestMapping("/api/servicios")
public class ServicioRestController {

    @Autowired
    private IServicioService servicioService;

    // 1. Obtener todos los servicios activos (para el RecyclerView de la app móvil)
    @GetMapping
    public ResponseEntity<List<Servicio>> listarServiciosActivos() {
        List<Servicio> servicios = servicioService.listarActivos();
        return ResponseEntity.ok(servicios);
    }

    // 2. Obtener un servicio por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerPorId(@PathVariable("id") Integer id) {
        return servicioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}