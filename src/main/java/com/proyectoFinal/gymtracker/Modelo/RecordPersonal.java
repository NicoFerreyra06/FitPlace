package com.proyectoFinal.gymtracker.Modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    @Column(nullable = false)
    private Double pesoMaximo;

    @Column(nullable = false)
    private LocalDate fechaLogro;
}
