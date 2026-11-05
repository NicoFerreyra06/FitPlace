package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.EjercicioSemilla;
import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial.MusculoSemilla;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import com.proyectoFinal.gymtracker.Services.CatalogoInicialService.ResultadoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatalogoInicialServiceTest {

    @Mock
    private MusculoRepository musculoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private CatalogoInicialService catalogoInicialService;

    @Captor
    private ArgumentCaptor<List<Ejercicio>> ejerciciosGuardados;

    private final Musculo pectoral = Musculo.builder().id(1L).nombre("Pectoral mayor").grupoMuscular(GrupoMuscular.PECHO).build();
    private final Musculo triceps = Musculo.builder().id(2L).nombre("Triceps braquial").grupoMuscular(GrupoMuscular.TRICEPS).build();

    @Test
    @DisplayName("Deberia crear solo los musculos y ejercicios que faltan, matcheando nombres sin tildes ni mayusculas")
    void creaSoloLoQueFalta() {
        when(musculoRepository.findAll()).thenReturn(List.of(pectoral, triceps));
        when(musculoRepository.save(any(Musculo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ejercicioRepository.findAll()).thenReturn(List.of(
                Ejercicio.builder().id(1L).nombre("Press banca").build()));

        CatalogoInicial catalogo = new CatalogoInicial(
                List.of(new MusculoSemilla("Tríceps Braquial", GrupoMuscular.TRICEPS),
                        new MusculoSemilla("Deltoides lateral", GrupoMuscular.HOMBRO_LATERAL)),
                List.of(new EjercicioSemilla("press  BANCA", "Repetido con otro formato", List.of("Pectoral mayor"), List.of()),
                        new EjercicioSemilla("Elevaciones laterales", "Con mancuernas", List.of("deltoides lateral"), null),
                        new EjercicioSemilla("Press francés", "Con barra Z", List.of("Tríceps braquial"), List.of("Pectoral mayor"))));

        ResultadoCarga resultado = catalogoInicialService.cargar(catalogo);

        assertEquals(new ResultadoCarga(1, 2, 0), resultado);
        verify(ejercicioRepository).saveAll(ejerciciosGuardados.capture());
        List<Ejercicio> guardados = ejerciciosGuardados.getValue();
        assertEquals(List.of("Elevaciones laterales", "Press francés"), guardados.stream().map(Ejercicio::getNombre).toList());
        assertEquals("Deltoides lateral", guardados.get(0).getMusculosPrincipales().get(0).getNombre());
        assertSame(triceps, guardados.get(1).getMusculosPrincipales().get(0));
        assertSame(pectoral, guardados.get(1).getMusculosSecundarios().get(0));
    }

    @Test
    @DisplayName("Deberia saltear un ejercicio con un musculo inexistente o sin musculos principales")
    void salteaEjerciciosInvalidos() {
        when(musculoRepository.findAll()).thenReturn(List.of(pectoral));
        when(ejercicioRepository.findAll()).thenReturn(List.of());

        CatalogoInicial catalogo = new CatalogoInicial(
                List.of(),
                List.of(new EjercicioSemilla("Aperturas", "Con mancuernas", List.of("Pectoral mayor"), List.of("Musculo que no existe")),
                        new EjercicioSemilla("Plancha", "Isometrico", List.of(), List.of())));

        ResultadoCarga resultado = catalogoInicialService.cargar(catalogo);

        assertEquals(new ResultadoCarga(0, 0, 2), resultado);
        verify(ejercicioRepository).saveAll(ejerciciosGuardados.capture());
        assertTrue(ejerciciosGuardados.getValue().isEmpty());
    }

    @Test
    @DisplayName("No deberia crear nada si todo el catalogo ya existe")
    void noDuplicaNada() {
        when(musculoRepository.findAll()).thenReturn(List.of(pectoral));
        when(ejercicioRepository.findAll()).thenReturn(List.of(Ejercicio.builder().id(1L).nombre("Press banca").build()));

        CatalogoInicial catalogo = new CatalogoInicial(
                List.of(new MusculoSemilla("Pectoral mayor", GrupoMuscular.PECHO)),
                List.of(new EjercicioSemilla("Press banca", "Barra en banco plano", List.of("Pectoral mayor"), List.of())));

        ResultadoCarga resultado = catalogoInicialService.cargar(catalogo);

        assertEquals(new ResultadoCarga(0, 0, 0), resultado);
        verify(musculoRepository, never()).save(any(Musculo.class));
    }

    @Test
    @DisplayName("Deberia normalizar tildes, mayusculas y espacios")
    void normalizar() {
        assertEquals("triceps braquial", CatalogoInicialService.normalizar("  Tríceps   BRAQUIAL "));
        assertEquals(CatalogoInicialService.normalizar("Cuádriceps"), CatalogoInicialService.normalizar("cuadriceps"));
    }
}
