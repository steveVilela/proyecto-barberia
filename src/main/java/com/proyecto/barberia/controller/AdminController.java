package com.proyecto.barberia.controller;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.service.IBarberoService;
import com.proyecto.barberia.service.ICitaService;
import com.proyecto.barberia.service.IServicioService;
import com.proyecto.barberia.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private IUsuarioService usuarioService;

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

    @GetMapping("/usuarios")
    public String listarUsuarios(HttpSession session, Model model) {
        if (!validarAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/listar";
    }
    
 // 1. Formulario Nuevo Usuario
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(HttpSession session, Model model) {
        if (!validarAdmin(session)) return "redirect:/login";
        model.addAttribute("usuarioForm", new Usuario());
        return "usuarios/formulario";
    }

    // 2. Formulario Editar Usuario
    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!validarAdmin(session)) return "redirect:/login";
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("usuarioForm", usuario);
        return "usuarios/formulario";
    }

    // 3. Guardar Usuario (Nuevo o Actualizado)
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("usuarioForm") Usuario usuarioForm,
                                 @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
                                 HttpSession session,
                                 RedirectAttributes flash) {
        if (!validarAdmin(session)) return "redirect:/login";

        try {
            // Si es edición y no se cambió la contraseña, mantener la que ya tenía
            if (usuarioForm.getIdUsuario() != null) {
                Usuario existente = usuarioService.buscarPorId(usuarioForm.getIdUsuario()).orElse(null);
                if (existente != null && (usuarioForm.getPassword() == null || usuarioForm.getPassword().isBlank())) {
                    usuarioForm.setPassword(existente.getPassword());
                }
            }

            usuarioService.registrarOActualizar(usuarioForm, archivoFoto);
            flash.addFlashAttribute("mensaje", "Usuario guardado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            flash.addFlashAttribute("error", "Error al procesar el usuario: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    // 4. Eliminar Usuario
    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes flash) {
        if (!validarAdmin(session)) return "redirect:/login";

        try {
            usuarioService.eliminar(id);
            flash.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el usuario.");
        }

        return "redirect:/admin/usuarios";
    }
    
    
}