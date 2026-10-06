package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private IServicioService servicioService;

    @Autowired
    private ICitaService citaService;

    @Autowired
    private IBarberoService barberoService;

    // 1. Ver catálogo de servicios
    @GetMapping("/mis-citas")
    public String panelServicios(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("servicios", servicioService.listarActivos());
        return "cliente/mis-citas";
    }

    // 2. Ver únicamente las reservas personales del cliente logueado
    @GetMapping("/reservas")
    public String misReservas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        List<Cita> misCitas = citaService.listarPorUsuario(usuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("citas", misCitas);
        return "cliente/reservas";
    }

    // 3. Formulario para que el cliente reserve su propio turno
    @GetMapping("/reservar")
    public String formularioReserva(@RequestParam(value = "idServicio", required = false) Integer idServicio,
                                    HttpSession session, 
                                    Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        Cita cita = new Cita();
        cita.setUsuario(usuario); // Asigna automáticamente al cliente logueado
        if (idServicio != null) {
            servicioService.buscarPorId(idServicio).ifPresent(cita::setServicio);
        }

        model.addAttribute("cita", cita);
        model.addAttribute("usuario", usuario);
        model.addAttribute("barberos", barberoService.listarActivos());
        model.addAttribute("servicios", servicioService.listarActivos());
        return "cliente/formulario_reserva";
    }

    // 4. Guardar la cita del cliente
    @PostMapping("/reservar/guardar")
    public String guardarReserva(@ModelAttribute("cita") Cita cita, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        cita.setUsuario(usuario); // Seguridad: asegura que la cita pertenezca a quien inició sesión
        cita.setEstado("PENDIENTE");
        citaService.registrarOActualizar(cita);
        return "redirect:/cliente/reservas";
    }

    // 5. Permitir al cliente cancelar su propia cita si está pendiente
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable("id") Integer id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        citaService.buscarPorId(id).ifPresent(cita -> {
            if (cita.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()) && "PENDIENTE".equalsIgnoreCase(cita.getEstado())) {
                citaService.cambiarEstado(id, "CANCELADA");
            }
        });
        return "redirect:/cliente/reservas";
    }

    @GetMapping("/perfil")
public String verPerfil(HttpSession session, Model model) {
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    if (usuario == null) return "redirect:/login";
    model.addAttribute("usuario", usuario);
    return "cliente/perfil";
}

@PostMapping("/perfil/actualizar-foto")
public String actualizarFotoPerfil(@RequestParam("archivoFoto") MultipartFile archivoFoto,
                                   HttpSession session,
                                   RedirectAttributes redirect) {
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    if (usuario == null) return "redirect:/login";

    if (!archivoFoto.isEmpty()) {
        try {
            // Reutiliza tu servicio de GCP existente
            String url = gcpStorageService.subirArchivo(archivoFoto);
            usuario.setFotoUrl(url);
            usuarioService.guardar(usuario);
            
            // Actualizar la sesión activa para que el menú muestre la foto al instante
            session.setAttribute("usuarioLogueado", usuario);
            redirect.addFlashAttribute("mensaje", "Foto de perfil actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }
    }
    return "redirect:/cliente/perfil";
}

}
