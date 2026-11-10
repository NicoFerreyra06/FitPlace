package com.proyectoFinal.gymtracker.Modelo;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Musculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // VARCHAR y no ENUM nativo de MySQL: así sumar un grupo muscular no requiere migrar la columna.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "grupo_muscular", nullable = false, length = 30)
    private GrupoMuscular grupoMuscular;

    @Column(name = "funcion_anatomica", length = 500)
    private String funcionAnatomica;
}
