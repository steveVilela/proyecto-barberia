package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.ICitaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/barbero")
public class BarberoController {

    @Autowired
    private ICitaService citaService;

    @GetMapping("/citas")
    public String panelBarbero(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"BARBERO".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("citasHoy", citaService.listarPorFecha(LocalDate.now()));
        return "barbero/citas";
    }
}