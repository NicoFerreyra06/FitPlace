package com.proyectoFinal.gymtracker.Modelo;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true)
    private String tokenCompartir; // Link para compartir con amigos o clientes

    // Solo si creador.rol == ENTRENADOR se debería permitir un precio mayor a 0
    private Double precio; 

    @Builder.Default
    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaRutina> dias = new ArrayList<>();
}
