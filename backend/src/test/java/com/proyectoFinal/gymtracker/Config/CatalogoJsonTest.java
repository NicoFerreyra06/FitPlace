package com.proyectoFinal.gymtracker.Config;

import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.EjercicioSemilla;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.MusculoSemilla;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.proyectoFinal.gymtracker.Services.CatalogoInicialService.normalizar;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Valida el catalogo.json real: si al sumar ejercicios queda un error (un músculo mal escrito,
 * un nombre repetido, una descripción muy larga), falla acá y no al levantar el back.
 */
public class CatalogoJsonTest {

    private static CatalogoInicial catalogo;

    @BeforeAll
    static void leerCatalogo() throws Exception {
        try (InputStream json = new ClassPathResource(CatalogoInicialRunner.RUTA_CATALOGO).getInputStream()) {
            catalogo = JsonMapper.builder().build().readValue(json, CatalogoInicial.class);
        }
    }

    @Test
    @DisplayName("Cada musculo tiene nombre, grupo muscular y no esta repetido")
    void musculosValidos() {
        List<String> errores = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (MusculoSemilla m : catalogo.musculos()) {
            if (m.nombre() == null || m.nombre().isBlank()) errores.add("Músculo sin nombre");
            else if (!vistos.add(normalizar(m.nombre()))) errores.add("Músculo repetido: " + m.nombre());
            if (m.grupoMuscular() == null) errores.add("Músculo sin grupo muscular: " + m.nombre());
        }
        assertTrue(errores.isEmpty(), String.join("\n", errores));
    }

    @Test
    @DisplayName("Cada ejercicio es unico, tiene descripcion valida y usa musculos definidos en el catalogo")
    void ejerciciosValidos() {
        Set<String> musculos = new HashSet<>();
        catalogo.musculos().forEach(m -> musculos.add(normalizar(m.nombre())));

        List<String> errores = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (EjercicioSemilla e : catalogo.ejercicios()) {
            if (e.nombre() == null || e.nombre().isBlank()) {
                errores.add("Ejercicio sin nombre");
                continue;
            }
            if (!vistos.add(normalizar(e.nombre()))) errores.add("Ejercicio repetido: " + e.nombre());
            if (e.nombre().length() > 255) errores.add("Nombre de más de 255 caracteres: " + e.nombre());
            if (e.descripcion() != null && e.descripcion().length() > 500)
                errores.add("Descripción de más de 500 caracteres: " + e.nombre());
            if (e.principales().isEmpty()) errores.add("Sin músculos principales: " + e.nombre());

            Stream.concat(e.principales().stream(), e.secundarios().stream())
                    .filter(nombre -> !musculos.contains(normalizar(nombre)))
                    .forEach(nombre -> errores.add("\"" + e.nombre() + "\" usa un músculo que no está en el catálogo: " + nombre));
        }
        assertTrue(errores.isEmpty(), String.join("\n", errores));
    }
}
