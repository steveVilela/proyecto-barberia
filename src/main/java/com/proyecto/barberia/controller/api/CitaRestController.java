package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    @Autowired
    private ICitaService citaService;

    @Autowired
    private IUsuarioService usuarioService;

    // 1. Obtener todas las reservas de un cliente específico (para su RecyclerView en Android)
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable("idUsuario") Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<Cita> citas = citaService.listarPorUsuario(usuarioOpt.get());
        return ResponseEntity.ok(citas);
    }

    // 2. Registrar una nueva reserva desde el teléfono móvil
    @PostMapping("/reservar")
    public ResponseEntity<?> registrarCita(@RequestBody Cita cita) {
        try {
            cita.setEstado("PENDIENTE");
            Cita nuevaCita = citaService.registrarOActualizar(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al registrar la cita: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 3. Modificar / Reprogramar una cita existente
    @PutMapping("/modificar/{idCita}")
    public ResponseEntity<?> modificarCita(@PathVariable("idCita") Integer idCita, @RequestBody Cita citaActualizada) {
        Optional<Cita> citaOpt = citaService.buscarPorId(idCita);
        if (citaOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Cita no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        try {
            Cita citaExistente = citaOpt.get();

            // Actualizar campos que envía la app móvil
            if (citaActualizada.getFecha() != null) citaExistente.setFecha(citaActualizada.getFecha());
            if (citaActualizada.getHora() != null) citaExistente.setHora(citaActualizada.getHora());
            if (citaActualizada.getBarbero() != null) citaExistente.setBarbero(citaActualizada.getBarbero());
            if (citaActualizada.getServicio() != null) citaExistente.setServicio(citaActualizada.getServicio());
            if (citaActualizada.getObservacion() != null) citaExistente.setObservacion(citaActualizada.getObservacion());

            Cita citaGuardada = citaService.registrarOActualizar(citaExistente);
            return ResponseEntity.ok(citaGuardada);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al modificar la cita: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 4. Cancelar una cita desde la app móvil
    @PutMapping("/cancelar/{idCita}")
    public ResponseEntity<?> cancelarCita(@PathVariable("idCita") Integer idCita) {
        Optional<Cita> citaOpt = citaService.buscarPorId(idCita);
        if (citaOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Cita no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        citaService.cambiarEstado(idCita, "CANCELADA");
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Cita cancelada con éxito");
        return ResponseEntity.ok(respuesta);
    }
}