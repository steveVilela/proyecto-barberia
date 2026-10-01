package com.proyecto.barberia.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ICloudStorageService {
    String subirArchivo(MultipartFile file, String carpeta) throws IOException;
    boolean eliminarArchivo(String urlPublica);
}