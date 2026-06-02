package com.proyectoFinal.gymtracker.Modelo;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuscripcionGimnasio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "id_gimnasio")
    private Gimnasio gimnasio;

    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    private double costo;

    @Column(name = "estado_suscripcion")
    @Enumerated(EnumType.STRING)
    private EstadoSuscripcion estadoSuscripcion;

    private double comisionApp;
}
