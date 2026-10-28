package com.proyectoFinal.gymtracker.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE ejercicio_rutina SET activo = false WHERE id = ?")
public class EjercicioRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dia_id", nullable = false)
    @JsonIgnore
    private DiaRutina dia;

    @ManyToOne
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    private Integer series;
    private Integer repeticiones;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean activo = true;
}
