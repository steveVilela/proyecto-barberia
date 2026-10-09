package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Cita;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import com.proyecto.barberia.service.IUsuarioService;
import com.proyecto.barberia.service.ICloudStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ICloudStorageService cloudStorageService;

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
        cita.setUsuario(usuario);
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

        cita.setUsuario(usuario);
        cita.setEstado("PENDIENTE");
        citaService.registrarOActualizar(cita);
        return "redirect:/cliente/reservas";
    }

    // 5. Cancelar cita
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

    // 6. Ver pantalla de Mi Perfil
    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "cliente/perfil";
    }

 // 7. Actualizar foto de perfil usando tu método registrarOActualizar
    @PostMapping("/perfil/actualizar-foto")
    public String actualizarFotoPerfil(@RequestParam("archivoFoto") MultipartFile archivoFoto,
                                       HttpSession session,
                                       RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        if (archivoFoto != null && !archivoFoto.isEmpty()) {
            try {
                // Tu propio servicio se encarga de subir la foto a GCP y actualizar la entidad
                Usuario usuarioActualizado = usuarioService.registrarOActualizar(usuario, archivoFoto);
                
                // Actualiza la sesión para reflejar la foto en la barra de navegación
                session.setAttribute("usuarioLogueado", usuarioActualizado);
                redirect.addFlashAttribute("mensaje", "Foto de perfil actualizada con éxito.");
            } catch (Exception e) {
                redirect.addFlashAttribute("error", "Error al procesar la foto: " + e.getMessage());
            }
        } else {
            redirect.addFlashAttribute("error", "Debes seleccionar un archivo de imagen.");
        }
        return "redirect:/cliente/perfil";
    }
    }