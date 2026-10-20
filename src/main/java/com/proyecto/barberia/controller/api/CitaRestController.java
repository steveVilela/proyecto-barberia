package com.proyecto.barberia.controller.api;

import com.proyecto.barberia.model.Barbero;
import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Servicio;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.repository.ICitaRepository;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import com.proyecto.barberia.service.IUsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
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
    private IBarberoService barberoService;

    @Autowired
    private IServicioService servicioService;

    @Autowired
    private IUsuarioService usuarioService;

    // Repositorio para la validación de cruce de horarios
    @Autowired
    private ICitaRepository citaRepository;

    // 1. Listar citas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Integer idUsuario) {

        Optional<Usuario> usuarioOpt =
                usuarioService.buscarPorId(idUsuario);

        if (usuarioOpt.isEmpty()) {
            return error(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado");
        }

        List<Map<String, Object>> resultado =
                citaService.listarPorUsuario(usuarioOpt.get())
                        .stream()
                        .map(this::convertirCita)
                        .toList();

        return ResponseEntity.ok(resultado);
    }

    // 2. Registrar una cita desde Android
    @PostMapping("/reservar")
    public ResponseEntity<?> registrarCita(
            @RequestBody Map<String, Object> datos) {

        try {
            Integer idUsuario = leerId(datos, "idUsuario");
            Integer idBarbero = leerId(datos, "idBarbero");
            Integer idServicio = leerId(datos, "idServicio");

            LocalDate fecha = LocalDate.parse(
                    leerTexto(datos, "fecha"));

            LocalTime hora = LocalTime.parse(
                    leerTexto(datos, "hora"));

            Usuario usuario = usuarioService.buscarPorId(idUsuario)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Usuario no encontrado"));

            Barbero barbero = barberoService.buscarPorId(idBarbero)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Barbero no encontrado"));

            Servicio servicio = servicioService.buscarPorId(idServicio)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Servicio no encontrado"));

            validarDisponibilidad(fecha, hora, barbero, servicio);

            // ---> CANDADO PARA EVITAR CRUCE DE HORARIOS DESDE ANDROID <---
            boolean ocupado = citaRepository.existeCitaActiva(barbero.getIdBarbero(), fecha, hora);
            if (ocupado) {
                return error(HttpStatus.CONFLICT, "Ese horario ya está ocupado por este barbero.");
            }

            Cita cita = new Cita();
            cita.setUsuario(usuario);
            cita.setBarbero(barbero);
            cita.setServicio(servicio);
            cita.setFecha(fecha);
            cita.setHora(hora);
            cita.setObservacion(
                    datos.get("observacion") == null
                            ? null
                            : datos.get("observacion").toString());
            cita.setEstado("PENDIENTE");

            Cita guardada =
                    citaService.registrarOActualizar(cita);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertirCita(guardada));

        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST,
                    e.getMessage() == null
                            ? "Datos inválidos"
                            : e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo registrar la cita");
        }
    }

    // 3. Modificar o reprogramar una cita
    @PutMapping("/modificar/{idCita}")
    public ResponseEntity<?> modificarCita(
            @PathVariable Integer idCita,
            @RequestBody Map<String, Object> datos) {

        Optional<Cita> citaOpt = citaService.buscarPorId(idCita);

        if (citaOpt.isEmpty()) {
            return error(HttpStatus.NOT_FOUND,
                    "Cita no encontrada");
        }

        try {
            Cita cita = citaOpt.get();

            if ("CANCELADA".equalsIgnoreCase(cita.getEstado())
                    || "FINALIZADA".equalsIgnoreCase(cita.getEstado())) {
                return error(HttpStatus.BAD_REQUEST,
                        "Esta cita no se puede modificar");
            }

            LocalDate fecha = cita.getFecha();
            LocalTime hora = cita.getHora();
            Barbero barbero = cita.getBarbero();
            Servicio servicio = cita.getServicio();

            if (datos.containsKey("fecha")) {
                fecha = LocalDate.parse(
                        leerTexto(datos, "fecha"));
            }

            if (datos.containsKey("hora")) {
                hora = LocalTime.parse(
                        leerTexto(datos, "hora"));
            }

            if (datos.containsKey("idBarbero")) {
                Integer idBarbero = leerId(datos, "idBarbero");

                barbero = barberoService.buscarPorId(idBarbero)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Barbero no encontrado"));
            }

            if (datos.containsKey("idServicio")) {
                Integer idServicio = leerId(datos, "idServicio");

                servicio = servicioService.buscarPorId(idServicio)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Servicio no encontrado"));
            }

            validarDisponibilidad(fecha, hora, barbero, servicio);

            // ---> CANDADO PARA EVITAR CRUCE AL REPROGRAMAR <---
            if (!cita.getFecha().equals(fecha) || !cita.getHora().equals(hora) || !cita.getBarbero().getIdBarbero().equals(barbero.getIdBarbero())) {
                boolean ocupado = citaRepository.existeCitaActiva(barbero.getIdBarbero(), fecha, hora);
                if (ocupado) {
                    return error(HttpStatus.CONFLICT, "Ese horario ya está ocupado por este barbero.");
                }
            }

            cita.setFecha(fecha);
            cita.setHora(hora);
            cita.setBarbero(barbero);
            cita.setServicio(servicio);

            if (datos.containsKey("observacion")) {
                cita.setObservacion(
                        datos.get("observacion") == null
                                ? null
                                : datos.get("observacion").toString());
            }

            Cita guardada =
                    citaService.registrarOActualizar(cita);

            return ResponseEntity.ok(convertirCita(guardada));

        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST,
                    e.getMessage() == null
                            ? "Datos inválidos"
                            : e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo modificar la cita");
        }
    }

    // 4. Cancelar una cita
    @PutMapping("/cancelar/{idCita}")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Integer idCita) {

        Optional<Cita> citaOpt = citaService.buscarPorId(idCita);

        if (citaOpt.isEmpty()) {
            return error(HttpStatus.NOT_FOUND,
                    "Cita no encontrada");
        }

        Cita cita = citaOpt.get();

        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())
                || "FINALIZADA".equalsIgnoreCase(cita.getEstado())) {
            return error(HttpStatus.BAD_REQUEST,
                    "Esta cita no se puede cancelar");
        }

        citaService.cambiarEstado(idCita, "CANCELADA");

        return ResponseEntity.ok(
                Map.of("mensaje", "Cita cancelada correctamente"));
    }

    // Validaciones compartidas para registrar y modificar
    private void validarDisponibilidad(
            LocalDate fecha,
            LocalTime hora,
            Barbero barbero,
            Servicio servicio) {

        if (!"ACTIVO".equalsIgnoreCase(barbero.getEstado())) {
            throw new IllegalArgumentException(
                    "El barbero no está activo");
        }

        if (!"ACTIVO".equalsIgnoreCase(servicio.getEstado())) {
            throw new IllegalArgumentException(
                    "El servicio no está activo");
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha no puede estar en el pasado");
        }

        if (fecha.equals(LocalDate.now())
                && hora.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException(
                    "La hora seleccionada ya pasó");
        }
    }

    // Convertir la entidad a JSON sencillo para Android
    private Map<String, Object> convertirCita(Cita cita) {
        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("idCita", cita.getIdCita());
        datos.put("idUsuario", cita.getUsuario().getIdUsuario());
        datos.put("idBarbero", cita.getBarbero().getIdBarbero());
        datos.put("nombreBarbero", cita.getBarbero().getNombre());
        datos.put("idServicio", cita.getServicio().getIdServicio());
        datos.put("nombreServicio", cita.getServicio().getNombre());
        datos.put("fecha", cita.getFecha().toString());
        datos.put("hora", cita.getHora().toString());
        datos.put("observacion", cita.getObservacion());
        datos.put("estado", cita.getEstado());

        return datos;
    }

    private Integer leerId(
            Map<String, Object> datos,
            String clave) {

        String valor = leerTexto(datos, clave);

        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo " + clave + " no es válido");
        }
    }

    private String leerTexto(
            Map<String, Object> datos,
            String clave) {

        Object valor = datos.get(clave);

        if (valor == null || valor.toString().isBlank()) {
            throw new IllegalArgumentException(
                    "Falta el campo " + clave);
        }

        return valor.toString();
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus estado,
            String mensaje) {

        return ResponseEntity.status(estado)
                .body(Map.of("mensaje", mensaje));
    }
}
