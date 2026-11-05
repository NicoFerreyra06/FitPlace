package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.EjercicioSemilla;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.MusculoSemilla;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Precarga el catálogo de músculos y ejercicios. Solo crea lo que falta: lo que ya existe no se toca,
 * así se puede correr en cada arranque sin duplicar nada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoInicialService {

    private final MusculoRepository musculoRepository;
    private final EjercicioRepository ejercicioRepository;

    public record ResultadoCarga(int musculosCreados, int ejerciciosCreados, int ejerciciosSalteados) {
    }

    @Transactional
    public ResultadoCarga cargar(CatalogoInicial catalogo) {
        Map<String, Musculo> musculos = new HashMap<>();
        musculoRepository.findAll().forEach(m -> musculos.put(normalizar(m.getNombre()), m));

        int musculosCreados = 0;
        for (MusculoSemilla semilla : catalogo.musculos()) {
            String clave = normalizar(semilla.nombre());
            if (musculos.containsKey(clave)) continue;

            if (semilla.grupoMuscular() == null) {
                log.warn("Catálogo inicial: se saltea el músculo \"{}\" porque no tiene grupo muscular", semilla.nombre());
                continue;
            }

            Musculo nuevo = musculoRepository.save(Musculo.builder()
                    .nombre(semilla.nombre().trim())
                    .grupoMuscular(semilla.grupoMuscular())
                    .build());
            musculos.put(clave, nuevo);
            musculosCreados++;
        }

        Set<String> ejerciciosExistentes = new HashSet<>();
        ejercicioRepository.findAll().forEach(e -> ejerciciosExistentes.add(normalizar(e.getNombre())));

        List<Ejercicio> nuevos = new ArrayList<>();
        int salteados = 0;
        for (EjercicioSemilla semilla : catalogo.ejercicios()) {
            // add() devuelve false si ya existe en la base o si está repetido en el archivo
            if (!ejerciciosExistentes.add(normalizar(semilla.nombre()))) continue;

            List<String> faltantes = new ArrayList<>();
            List<Musculo> principales = buscarMusculos(semilla.principales(), musculos, faltantes);
            List<Musculo> secundarios = buscarMusculos(semilla.secundarios(), musculos, faltantes);

            if (!faltantes.isEmpty() || principales.isEmpty()) {
                String motivo = faltantes.isEmpty() ? "no tiene músculos principales" : "músculos inexistentes " + faltantes;
                log.warn("Catálogo inicial: se saltea el ejercicio \"{}\": {}", semilla.nombre(), motivo);
                salteados++;
                continue;
            }

            nuevos.add(Ejercicio.builder()
                    .nombre(semilla.nombre().trim())
                    .descripcion(semilla.descripcion())
                    .musculosPrincipales(principales)
                    .musculosSecundarios(secundarios)
                    .build());
        }

        ejercicioRepository.saveAll(nuevos);
        return new ResultadoCarga(musculosCreados, nuevos.size(), salteados);
    }

    /**
     * Compara nombres sin tildes, mayúsculas ni espacios de más, igual que la collation de MySQL:
     * "Tríceps braquial" y "triceps  braquial" son el mismo músculo y no chocan con el nombre único.
     */
    public static String normalizar(String nombre) {
        String sinTildes = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private List<Musculo> buscarMusculos(List<String> nombres, Map<String, Musculo> musculos, List<String> faltantes) {
        List<Musculo> encontrados = new ArrayList<>();
        for (String nombre : nombres) {
            Musculo musculo = musculos.get(normalizar(nombre));
            if (musculo == null) {
                faltantes.add(nombre);
            } else {
                encontrados.add(musculo);
            }
        }
        return encontrados;
    }
}
