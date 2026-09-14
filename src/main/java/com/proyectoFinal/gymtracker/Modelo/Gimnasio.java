package com.proyectoFinal.gymtracker.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE gimnasio set activo = false where id=?")
@SQLRestriction("activo = true")
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

    @Builder.Default
    @OneToMany(mappedBy = "gimnasio")
    @JsonIgnore
    private List<Usuario> miembros = new ArrayList<>();

    @Column(nullable = false)
    private LocalTime horarioApertura;
    @Column(nullable = false)
    private LocalTime horarioCierre;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @NotEmpty
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "gimnasio_dias", joinColumns = @JoinColumn(name = "gimnasio_id"))
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> diasAbierto = new ArrayList<>();

}
