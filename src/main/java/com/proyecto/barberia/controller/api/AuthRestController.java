package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private IUsuarioService usuarioService;

    // 1. Iniciar sesión
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");

        if (correo == null || password == null) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Correo y contraseña requeridos");
            return ResponseEntity.badRequest().body(error);
        }

        Usuario usuario = usuarioService.login(correo, password);

        if (usuario != null) {
            usuario.setPassword(null); // Ocultar contraseña por seguridad
            return ResponseEntity.ok(usuario);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Credenciales incorrectas o usuario inactivo");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // 2. Registro público de nuevos clientes desde Android
    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody Usuario nuevoUsuario) {
        // Validar si el correo ya existe
        Optional<Usuario> usuarioExistente = usuarioService.buscarPorCorreo(nuevoUsuario.getCorreo());
        if (usuarioExistente.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "El correo ya se encuentra registrado");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Valores por defecto para registros desde la app móvil
            nuevoUsuario.setRol("CLIENTE");
            nuevoUsuario.setEstado("ACTIVO");

            // Pasamos null en foto porque al registrarse aún no sube foto
            Usuario usuarioCreado = usuarioService.registrarOActualizar(nuevoUsuario, null);
            usuarioCreado.setPassword(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al crear la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}