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

import java.io.IOException;
import java.util.UUID;

@Service
public class CloudStorageServiceImpl implements ICloudStorageService {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${gcp.credentials.path}")
    private Resource credentialsPath;

    private Storage obtenerStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsPath.getInputStream());
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    @Override
    public String subirArchivo(MultipartFile file, String carpeta) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Storage storage = obtenerStorage();

        // Extraer extensión y generar nombre único
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = carpeta + "/" + UUID.randomUUID().toString() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        // Subir archivo al bucket
        storage.create(blobInfo, file.getBytes());

        // Retornar la URL pública accesible para el navegador y Android
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    @Override
    public boolean eliminarArchivo(String urlPublica) {
        if (urlPublica == null || urlPublica.isBlank()) {
            return false;
        }

        try {
            Storage storage = obtenerStorage();
            // Extraer la ruta del blob desde la URL pública
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