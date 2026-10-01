package com.proyecto.barberia.service;

import com.proyecto.barberia.model.Usuario;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Integer id);
    Optional<Usuario> buscarPorCorreo(String correo);
    Usuario registrarOActualizar(Usuario usuario, MultipartFile foto) throws IOException;
    Usuario login(String correo, String password);
    void eliminar(Integer id);
}