package com.proyectoFinal.gymtracker.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double pesoLevantado;
    private Integer repeticionesLogradas;

    @ManyToOne
    @JoinColumn(name = "entrenamiento_log_id", nullable = false)
    @JsonIgnore
    private EntrenamientoLog entrenamientoLog;

    @ManyToOne
    @JoinColumn(name = "ejercicio_rutina_id", nullable = false)
    private EjercicioRutina ejercicioRutina;

}
