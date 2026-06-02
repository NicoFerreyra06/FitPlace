package com.proyectoFinal.gymtracker.Modelo;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gimnasio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String cadena;

    private String direccion;

    //falta listado de horarios

    @Column(name = "costo_mensual")
    private double costoMensual;

    @OneToMany(mappedBy = "gimnasio")
    private List<SuscripcionGimnasio> suscripciones;


}
