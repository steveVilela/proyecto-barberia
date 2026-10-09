package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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

    // 3. Subir foto de perfil desde Android o Postman
    @PostMapping("/{id}/foto")
    public ResponseEntity<?> subirFotoPerfil(@PathVariable("id") Integer id,
                                             @RequestParam("foto") MultipartFile foto) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El archivo de imagen es requerido"));
        }

        try {
            Usuario usuario = usuarioOpt.get();
            // Tu propio método se encarga de subir el archivo a GCP y guardar el usuario
            Usuario usuarioActualizado = usuarioService.registrarOActualizar(usuario, foto);
            
            usuarioActualizado.setPassword(null);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}