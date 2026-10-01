package com.proyecto.barberia.service.impl;

import com.proyecto.barberia.model.Usuario;
import com.proyecto.barberia.repository.IUsuarioRepository;
import com.proyecto.barberia.service.ICloudStorageService;
import com.proyecto.barberia.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private ICloudStorageService cloudStorageService;

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    public Usuario registrarOActualizar(Usuario usuario, MultipartFile foto) throws IOException {
        if (foto != null && !foto.isEmpty()) {
            if (usuario.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(usuario.getFotoUrl());
            }
            String url = cloudStorageService.subirArchivo(foto, "usuarios");
            usuario.setFotoUrl(url);
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        if (usuario.getRol() == null) {
            usuario.setRol("CLIENTE");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario login(String correo, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            if (u.getPassword().equals(password) && "ACTIVO".equalsIgnoreCase(u.getEstado())) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void eliminar(Integer id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            if (u.getFotoUrl() != null) {
                cloudStorageService.eliminarArchivo(u.getFotoUrl());
            }
            usuarioRepository.deleteById(id);
        });
    }
}