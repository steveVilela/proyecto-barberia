package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private IUsuarioService usuarioService;

    // 1. REGISTRAR USUARIO
    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(
            @RequestBody Usuario solicitud) {

        Map<String, Object> respuesta = new HashMap<>();

        if (solicitud.getNombre() == null
                || solicitud.getNombre().trim().isEmpty()
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().trim().isEmpty()
                || solicitud.getPassword() == null
                || solicitud.getPassword().isEmpty()) {

            respuesta.put("mensaje",
                    "Nombre, correo y contraseña son obligatorios.");

            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            Optional<Usuario> existente =
                    usuarioService.buscarPorCorreo(
                            solicitud.getCorreo().trim());

            if (existente.isPresent()) {
                respuesta.put("mensaje",
                        "Ese correo ya está registrado.");
                return ResponseEntity.badRequest().body(respuesta);
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(solicitud.getNombre().trim());
            nuevo.setCorreo(solicitud.getCorreo().trim());
            nuevo.setPassword(solicitud.getPassword());
            nuevo.setTelefono(solicitud.getTelefono());
            nuevo.setRol("CLIENTE");
            nuevo.setEstado("ACTIVO");

            Usuario registrado =
                    usuarioService.registrarOActualizar(nuevo, null);

            respuesta.put("mensaje", "Cuenta creada correctamente.");
            respuesta.put("idUsuario", registrado.getIdUsuario());
            respuesta.put("nombre", registrado.getNombre());
            respuesta.put("correo", registrado.getCorreo());
            respuesta.put("rol", registrado.getRol());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(respuesta);

        } catch (Exception e) {
            respuesta.put("mensaje",
                    "No se pudo registrar el usuario.");
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    // 2. CONSULTAR PERFIL
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable("id") Integer id) {

        Optional<Usuario> usuarioOpt =
                usuarioService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(datosPerfil(usuarioOpt.get()));
    }

    // 3. LISTAR CLIENTES
    @GetMapping
    public ResponseEntity<List<Usuario>> listarClientes() {

        List<Usuario> clientes = usuarioService.listarTodos()
                .stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRol()))
                .peek(u -> u.setPassword(null))
                .toList();

        return ResponseEntity.ok(clientes);
    }

    // 4. ACTUALIZAR PERFIL
    @PutMapping("/perfil/{id}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable Integer id,
            @RequestBody Map<String, String> datos) {

        Optional<Usuario> usuarioOpt =
                usuarioService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String nombre = datos.get("nombre");
        String correo = datos.get("correo");
        String telefono = datos.get("telefono");

        if (nombre == null || nombre.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El nombre y el correo son obligatorios."));
        }

        Usuario usuario = usuarioOpt.get();

        Optional<Usuario> existente =
                usuarioService.buscarPorCorreo(correo.trim());

        if (existente.isPresent()
                && !existente.get().getIdUsuario().equals(id)) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "Ese correo ya está registrado."));
        }

        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correo.trim());
        usuario.setTelefono(telefono);

        try {
            Usuario actualizado =
                    usuarioService.registrarOActualizar(usuario, null);

            return ResponseEntity.ok(datosPerfil(actualizado));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "No se pudo actualizar el perfil."));
        }
    }

    // 5. CAMBIAR CONTRASEÑA
    @PutMapping("/password/{id}")
    public ResponseEntity<?> cambiarPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> datos) {

        Optional<Usuario> usuarioOpt =
                usuarioService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String actual = datos.get("passwordActual");
        String nueva = datos.get("passwordNueva");
        String confirmar = datos.get("passwordConfirmar");

        Usuario usuario = usuarioOpt.get();

        if (actual == null || !actual.equals(usuario.getPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "La contraseña actual es incorrecta."));
        }

        if (nueva == null || nueva.length() < 8) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "La nueva contraseña debe tener al menos 8 caracteres."));
        }

        if (!nueva.equals(confirmar)) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "Las contraseñas nuevas no coinciden."));
        }

        usuario.setPassword(nueva);

        try {
            usuarioService.registrarOActualizar(usuario, null);

            return ResponseEntity.ok(
                    Map.of("mensaje",
                            "Contraseña actualizada correctamente."));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "No se pudo cambiar la contraseña."));
        }
    }

    // 6. SUBIR FOTO DE PERFIL
    @PostMapping(
            value = "/{id}/foto",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFotoPerfil(
            @PathVariable("id") Integer id,
            @RequestParam("foto") MultipartFile foto) {

        Optional<Usuario> usuarioOpt =
                usuarioService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El archivo de imagen es requerido."));
        }

        if (foto.getContentType() == null
                || !foto.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje",
                            "El archivo debe ser una imagen."));
        }

        try {
            Usuario usuario = usuarioOpt.get();

            Usuario actualizado =
                    usuarioService.registrarOActualizar(usuario, foto);

            return ResponseEntity.ok(datosPerfil(actualizado));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("mensaje",
                            "No se pudo subir la foto."));
        }
    }

    // 7. DEVOLVER DATOS SIN EXPONER LA CONTRASEÑA
    private Map<String, Object> datosPerfil(Usuario usuario) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("idUsuario", usuario.getIdUsuario());
        datos.put("nombre", usuario.getNombre());
        datos.put("correo", usuario.getCorreo());
        datos.put("telefono", usuario.getTelefono());
        datos.put("fotoUrl", usuario.getFotoUrl());
        datos.put("rol", usuario.getRol());
        datos.put("estado", usuario.getEstado());

        return datos;
    }
}
