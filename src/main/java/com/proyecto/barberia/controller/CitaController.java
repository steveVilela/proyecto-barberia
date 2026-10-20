package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.repository.ICitaRepository;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import com.proyecto.barberia.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private ICitaService citaService;

    @Autowired
    private IBarberoService barberoService;

    @Autowired
    private IServicioService servicioService;

    @Autowired
    private IUsuarioService usuarioService;

    // Repositorio inyectado para bloquear cruces en la web
    @Autowired
    private ICitaRepository citaRepository;

    // 1. Listar citas (Para ADMIN o JEFE LOCAL / BARBERO)
    @GetMapping
    public String listarCitas(@RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                              HttpSession session, 
                              Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Cita> citas;
        if (fecha != null) {
            citas = citaService.listarPorFecha(fecha);
            model.addAttribute("fechaFiltro", fecha);
        } else {
            citas = citaService.listarTodas();
        }

        model.addAttribute("citas", citas);
        model.addAttribute("usuario", usuario);
        return "citas/listar";
    }

    // 2. Formulario para nueva Cita (Administración / Manual)
    @GetMapping("/nueva")
    public String nuevaCita(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/citas";
        }

        // Filtrar únicamente los usuarios con rol CLIENTE y en estado ACTIVO
        List<Usuario> clientes = usuarioService.listarTodos().stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRol()) && "ACTIVO".equalsIgnoreCase(u.getEstado()))
                .toList();

        model.addAttribute("cita", new Cita());
        model.addAttribute("barberos", barberoService.listarActivos());
        model.addAttribute("servicios", servicioService.listarActivos());
        model.addAttribute("usuarios", clientes);
        return "citas/formulario";
    }

    // 3. Formulario para editar / reprogramar Cita existente (Web Admin)
    @GetMapping("/editar/{id}")
    public String editarCita(@PathVariable("id") Integer id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/citas";
        }

        Cita cita = citaService.buscarPorId(id).orElse(null);
        if (cita == null) {
            return "redirect:/citas";
        }

        List<Usuario> clientes = usuarioService.listarTodos().stream()
                .filter(u -> "CLIENTE".equalsIgnoreCase(u.getRol()) && "ACTIVO".equalsIgnoreCase(u.getEstado()))
                .toList();

        model.addAttribute("cita", cita);
        model.addAttribute("barberos", barberoService.listarActivos());
        model.addAttribute("servicios", servicioService.listarActivos());
        model.addAttribute("usuarios", clientes);
        return "citas/formulario";
    }

    // 4. Guardar o Modificar cita (Web)
    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute("cita") Cita cita, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // ---> CANDADO PARA LA PÁGINA WEB <---
        if (!"CANCELADA".equalsIgnoreCase(cita.getEstado()) && !"FINALIZADA".equalsIgnoreCase(cita.getEstado())) {
            boolean ocupado = citaRepository.existeCitaActiva(cita.getBarbero().getIdBarbero(), cita.getFecha(), cita.getHora());
            
            // Verificamos si es una inserción nueva o si está editando y cambiando el horario
            if (ocupado) {
                if (cita.getIdCita() == null) {
                    redirectAttributes.addFlashAttribute("error", "Error: Ese horario ya está ocupado por el barbero seleccionado.");
                    return "redirect:/citas/nueva";
                } else {
                    Cita citaOriginal = citaService.buscarPorId(cita.getIdCita()).orElse(null);
                    if (citaOriginal != null && (!citaOriginal.getFecha().equals(cita.getFecha()) || !citaOriginal.getHora().equals(cita.getHora()) || !citaOriginal.getBarbero().getIdBarbero().equals(cita.getBarbero().getIdBarbero()))) {
                        redirectAttributes.addFlashAttribute("error", "Error: Ese horario ya está ocupado. No se puede reprogramar a esa hora.");
                        return "redirect:/citas/editar/" + cita.getIdCita();
                    }
                }
            }
        }

        // Si es una cita nueva que aún no tiene estado asignado, se inicia en PENDIENTE
        if (cita.getIdCita() == null && cita.getEstado() == null) {
            cita.setEstado("PENDIENTE");
        }

        citaService.registrarOActualizar(cita);
        redirectAttributes.addFlashAttribute("exito", "Cita guardada correctamente.");
        return "redirect:/citas";
    }

    // 5. Cambiar estado rápido: CONFIRMADA, FINALIZADA, CANCELADA
    @GetMapping("/estado/{id}/{nuevoEstado}")
    public String cambiarEstado(@PathVariable("id") Integer id, 
                                @PathVariable("nuevoEstado") String nuevoEstado,
                                HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        citaService.cambiarEstado(id, nuevoEstado.toUpperCase());
        return "redirect:/citas";
    }
}
