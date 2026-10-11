package com.proyecto.barberia.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.proyecto.barberia.service.ICloudStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class CloudStorageServiceImpl implements ICloudStorageService {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    // Lee el contenido JSON crudo desde variables de entorno (Azure / Prod)
    @Value("${gcp.credentials.json:}")
    private String credentialsJson;

    // Ruta de fallback local si existe el archivo (Desarrollo local)
    @Value("${gcp.credentials.path:classpath:gcp-credentials.json}")
    private Resource credentialsPath;

    private Storage obtenerStorage() throws IOException {
        GoogleCredentials credentials;

        // 1. Prioridad: Variable de entorno directa (Azure)
        if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
            credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
            );
        } 
        // 2. Alternativa: Archivo local si está presente
        else if (credentialsPath != null && credentialsPath.exists()) {
            credentials = GoogleCredentials.fromStream(credentialsPath.getInputStream());
        } 
        // 3. Credenciales por defecto del entorno
        else {
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    @Override
    public String subirArchivo(MultipartFile file, String carpeta) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Storage storage = obtenerStorage();

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = carpeta + "/" + UUID.randomUUID().toString() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    @Override
    public boolean eliminarArchivo(String urlPublica) {
        if (urlPublica == null || urlPublica.isBlank()) {
            return false;
        }

        try {
            Storage storage = obtenerStorage();
            String prefijo = String.format("https://storage.googleapis.com/%s/", bucketName);
            if (urlPublica.startsWith(prefijo)) {
                String blobName = urlPublica.replace(prefijo, "");
                return storage.delete(BlobId.of(bucketName, blobName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}