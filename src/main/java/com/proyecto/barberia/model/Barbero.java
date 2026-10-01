package com.proyecto.barberia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "barbero")
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_barbero")
    private Integer idBarbero;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String especialidad;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(nullable = false, length = 20)
    private String estado; // 'ACTIVO' o 'INACTIVO'
}