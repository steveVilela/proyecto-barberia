package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private IUsuarioService usuarioService;

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
            // No devolvemos la contraseña al cliente por seguridad
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Credenciales incorrectas o usuario inactivo");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}