package com.proyectoFinal.gymtracker.DTO.Catalogo;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;

import java.util.List;

/**
 * Contenido de resources/catalogo/catalogo.json: músculos y ejercicios que el back precarga al iniciar.
 * Los ejercicios referencian a los músculos por nombre (no por id), así el archivo sirve en cualquier base.
 */
public record CatalogoInicial(List<MusculoSemilla> musculos, List<EjercicioSemilla> ejercicios) {

    public CatalogoInicial {
        musculos = musculos == null ? List.of() : musculos;
        ejercicios = ejercicios == null ? List.of() : ejercicios;
    }

    public record MusculoSemilla(String nombre, GrupoMuscular grupoMuscular) {
    }

    public record EjercicioSemilla(String nombre,
                                   String descripcion,
                                   List<String> principales,
                                   List<String> secundarios) {

        public EjercicioSemilla {
            principales = principales == null ? List.of() : principales;
            secundarios = secundarios == null ? List.of() : secundarios;
        }
    }
}
