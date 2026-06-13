package com.proyectoFinal.gymtracker.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Enum.MetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuscripcionGimnasio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_gimnasio")
    private Gimnasio gimnasio;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    private double costo;

    @Column(name = "metodo_pago")
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Column(name = "estado_suscripcion")
    @Enumerated(EnumType.STRING)
    private EstadoSuscripcion estadoSuscripcion;

    private double comisionApp;
}
