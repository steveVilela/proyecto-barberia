package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private IServicioService servicioService;

    @GetMapping("/mis-citas")
    public String panelCliente(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("servicios", servicioService.listarActivos());
        return "cliente/mis-citas";
    }
}