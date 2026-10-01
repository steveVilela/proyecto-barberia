package com.proyecto.barberia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 20)
    private String rol; // 'ADMIN' o 'CLIENTE'

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(nullable = false, length = 20)
    private String estado; // 'ACTIVO' o 'INACTIVO'
}