package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
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

    // Listar citas (Para ADMIN o JEFE LOCAL / BARBERO)
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

 // Formulario para nueva Cita (Administración / Manual)
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
    
    // Guardar cita
    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute("cita") Cita cita, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        citaService.registrarOActualizar(cita);
        return "redirect:/citas";
    }

    // Cambiar estado rápido: CONFIRMADA, FINALIZADA, CANCELADA
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