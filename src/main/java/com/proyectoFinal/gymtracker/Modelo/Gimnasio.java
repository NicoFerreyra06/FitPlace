package com.proyectoFinal.gymtracker.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gimnasio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Double precioCuota;

    @ManyToOne
    @JoinColumn(nullable = false, name = "admin_id")
    private Usuario admin;

    @OneToMany(mappedBy = "gimnasio")
    private List<Usuario> miembros;

    @Column(nullable = false)
    private LocalTime horarioApertura;
    @Column(nullable = false)
    private LocalTime horarioCierre;

    @NotEmpty
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "gimnasio_dias", joinColumns = @JoinColumn(name = "gimnasio_id"))
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> diasAbierto;

}
