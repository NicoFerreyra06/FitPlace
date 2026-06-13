package com.proyectoFinal.gymtracker.Modelo;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToMany
    @JoinTable(
        name = "ejercicio_musculo_principal",
        joinColumns = @JoinColumn(name = "ejercicio_id"),
        inverseJoinColumns = @JoinColumn(name = "musculo_id")
    )
    private List<Musculo> musculosPrincipales;

    @ManyToMany
    @JoinTable(
        name = "ejercicio_musculo_secundario",
        joinColumns = @JoinColumn(name = "ejercicio_id"),
        inverseJoinColumns = @JoinColumn(name = "musculo_id")
    )
    private List<Musculo> musculosSecundarios;
}
