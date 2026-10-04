package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Servicio;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    @Autowired
    private IServicioService servicioService;

    // Listar servicios (Accesible por Admin, Jefe Local y Cliente)
    @GetMapping
    public String listar(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Si es Admin muestra todos (activos e inactivos), si no, solo los activos
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            model.addAttribute("servicios", servicioService.listarTodos());
        } else {
            model.addAttribute("servicios", servicioService.listarActivos());
        }
        
        model.addAttribute("usuario", usuario);
        return "servicios/listar";
    }

    // Formulario de nuevo servicio (Solo ADMIN)
    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/servicios";
        }
        model.addAttribute("servicio", new Servicio());
        return "servicios/formulario";
    }

    // Formulario de edición (Solo ADMIN)
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/servicios";
        }
        servicioService.buscarPorId(id).ifPresent(s -> model.addAttribute("servicio", s));
        return "servicios/formulario";
    }

    // Guardar o Actualizar servicio con foto en GCP
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("servicio") Servicio servicio,
                          @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
                          HttpSession session) throws IOException {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }
        servicioService.guardar(servicio, archivoFoto);
        return "redirect:/servicios";
    }

    // Eliminar servicio (Solo ADMIN)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }
        servicioService.eliminar(id);
        return "redirect:/servicios";
    }
}