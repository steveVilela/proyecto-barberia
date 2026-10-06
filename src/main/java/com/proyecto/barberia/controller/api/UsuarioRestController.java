package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private IUsuarioService usuarioService;

    // 1. Obtener perfil de un usuario por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable("id") Integer id) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(null); // Seguridad: no exponer la contraseña
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    // 2. Listar clientes (opcional para administración)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarClientes() {
        List<Usuario> clientes = usuarioService.listarTodos().stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRol()))
                .peek(u -> u.setPassword(null))
                .toList();
        return ResponseEntity.ok(clientes);
    }
}