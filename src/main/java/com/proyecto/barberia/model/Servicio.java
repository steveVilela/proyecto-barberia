package com.proyecto.barberia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio")
    private Double precio;
    
    @Column(nullable = false)
    private Integer duracion; // en minutos

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(nullable = false, length = 20)
    private String estado; // 'ACTIVO' o 'INACTIVO'
}