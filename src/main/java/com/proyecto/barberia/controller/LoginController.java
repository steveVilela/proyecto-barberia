package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping({"/", "/login"})
    public String verLogin(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion != null) {
            return redireccionarPorRol(usuarioSesion.getRol());
        }
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(@RequestParam("correo") String correo,
                             @RequestParam("password") String password,
                             HttpSession session,
                             Model model) {
        Usuario usuario = usuarioService.login(correo, password);

        if (usuario != null) {
            session.setAttribute("usuarioLogueado", usuario);
            return redireccionarPorRol(usuario.getRol());
        }

        model.addAttribute("error", "Correo o contraseña incorrectos, o usuario inactivo.");
        return "login";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    private String redireccionarPorRol(String rol) {
        if ("ADMIN".equalsIgnoreCase(rol)) {
            return "redirect:/admin/dashboard";
        } else if ("BARBERO".equalsIgnoreCase(rol)) {
            return "redirect:/barbero/citas";
        } else {
            return "redirect:/cliente/mis-citas";
        }
    }
}