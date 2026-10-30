package com.proyectoFinal.gymtracker.Modelo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE rutina SET activa = false WHERE id = ?")
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

    @Column(nullable = false)
    @Builder.Default
    private boolean esPublica = false; 

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean activa = true;

    @Builder.Default
    @SQLRestriction("activo = true")
    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<DiaRutina> dias = new ArrayList<>();
}
