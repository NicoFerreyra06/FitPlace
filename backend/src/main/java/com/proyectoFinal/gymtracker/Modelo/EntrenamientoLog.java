package com.proyectoFinal.gymtracker.Modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = @Index(name = "idx_entrenamiento_usuario_fecha", columnList = "usuario_id, fecha"))
public class EntrenamientoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "rutina_ejecutada_id")
    private Rutina rutinaEjecutada;

    @Builder.Default
    @OneToMany(mappedBy = "entrenamientoLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MarcaEjercicio> marcas = new ArrayList<>();
}
