package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IBarberoService barberoService;

    @Autowired
    private IServicioService servicioService;

    @Autowired
    private ICitaService citaService;

    private boolean validarAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        return usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }

        Usuario admin = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", admin);

        // Métricas para tarjetas
        model.addAttribute("totalBarberos", barberoService.listarActivos().size());
        model.addAttribute("totalServicios", servicioService.listarActivos().size());
        model.addAttribute("citasHoy", citaService.listarPorFecha(LocalDate.now()).size());
        model.addAttribute("ultimasCitas", citaService.listarUltimasCinco());

        return "admin/dashboard";
    }
}