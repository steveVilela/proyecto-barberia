package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Barbero;
import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IBarberoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/barberos")
public class BarberoViewController {

    @Autowired
    private IBarberoService barberoService;

    private boolean validarAdmin(HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRol());
    }

    @GetMapping
    public String listar(HttpSession session, Model model) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("barberos", barberoService.listarTodos());
        return "barberos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("barbero", new Barbero());
        return "barberos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }
        return barberoService.buscarPorId(id)
                .map(barbero -> {
                    model.addAttribute("barbero", barbero);
                    return "barberos/formulario";
                })
                .orElse("redirect:/barberos");
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("barbero") Barbero barbero,
                          @RequestParam("archivoFoto") MultipartFile archivoFoto,
                          HttpSession session,
                          RedirectAttributes flash) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }
        try {
            barberoService.guardar(barbero, archivoFoto);
            flash.addFlashAttribute("mensaje", "Barbero guardado con éxito.");
        } catch (Exception e) {
            e.printStackTrace();
            flash.addFlashAttribute("error", "Error al procesar la imagen en Cloud Storage: " + e.getMessage());
        }
        return "redirect:/barberos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes flash) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }
        barberoService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Barbero eliminado correctamente.");
        return "redirect:/barberos";
    }
}