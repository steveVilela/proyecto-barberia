package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Barbero;
import com.proyecto.barberia.service.IBarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/barberos")
public class BarberoRestController {

    @Autowired
    private IBarberoService barberoService;

    @GetMapping
    public ResponseEntity<List<Barbero>> listarActivos() {
        return ResponseEntity.ok(barberoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barbero> buscarPorId(@PathVariable("id") Integer id) {
        return barberoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}