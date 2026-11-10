package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.MusculoRequest;
import com.proyectoFinal.gymtracker.DTO.Response.MusculoResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MusculoServiceTest {

    @Mock
    private MusculoRepository musculoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private MusculoService musculoService;

    private Musculo musculo;

    @BeforeEach
    public void setUp() {
        musculo = Musculo.builder()
                .id(1L)
                .nombre("Pectoral mayor")
                .grupoMuscular(GrupoMuscular.PECHO)
                .funcionAnatomica("Flexión, aducción y rotación interna del húmero")
                .build();
    }

    @Test
    @DisplayName("Debería crear el músculo recortando espacios en el nombre y la función anatómica")
    void shouldCreateMusculoSuccessful() {
        MusculoRequest request = MusculoRequest.builder()
                .nombre("  Deltoides lateral  ")
                .grupoMuscular(GrupoMuscular.HOMBRO_LATERAL)
                .funcionAnatomica("  Abducción del húmero  ")
                .build();

        when(musculoRepository.existsByNombreIgnoreCase("Deltoides lateral")).thenReturn(false);
        when(musculoRepository.save(any(Musculo.class))).thenAnswer(inv -> inv.getArgument(0));

        MusculoResponse response = musculoService.addMusculo(request);

        assertEquals("Deltoides lateral", response.getNombre());
        assertEquals("Abducción del húmero", response.getFuncionAnatomica());
        assertEquals(GrupoMuscular.HOMBRO_LATERAL, response.getGrupoMuscular());
    }

    @Test
    @DisplayName("Debería rechazar un músculo con nombre repetido")
    void shouldThrowWhenNombreAlreadyExists() {
        MusculoRequest request = MusculoRequest.builder()
                .nombre("Pectoral mayor")
                .grupoMuscular(GrupoMuscular.PECHO)
                .build();

        when(musculoRepository.existsByNombreIgnoreCase("Pectoral mayor")).thenReturn(true);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> musculoService.addMusculo(request));

        assertTrue(exception.getMessage().contains("Ya existe un músculo"));
        verify(musculoRepository, never()).save(any(Musculo.class));
    }

    @Test
    @DisplayName("Debería actualizar el músculo correctamente")
    void shouldUpdateMusculoSuccessful() {
        MusculoRequest request = MusculoRequest.builder()
                .nombre("Pectoral mayor")
                .grupoMuscular(GrupoMuscular.PECHO)
                .funcionAnatomica("  Aducción del húmero  ")
                .build();

        when(musculoRepository.findById(1L)).thenReturn(Optional.of(musculo));
        when(musculoRepository.existsByNombreIgnoreCaseAndIdNot("Pectoral mayor", 1L)).thenReturn(false);
        when(musculoRepository.save(any(Musculo.class))).thenAnswer(inv -> inv.getArgument(0));

        MusculoResponse response = musculoService.updateMusculo(request, 1L);

        assertEquals("Aducción del húmero", response.getFuncionAnatomica());
        verify(musculoRepository, times(1)).save(musculo);
    }

    @Test
    @DisplayName("Debería rechazar la actualización si el nombre pertenece a otro músculo")
    void shouldThrowWhenUpdatingWithDuplicatedNombre() {
        MusculoRequest request = MusculoRequest.builder()
                .nombre("Dorsal ancho")
                .grupoMuscular(GrupoMuscular.DORSAL)
                .build();

        when(musculoRepository.findById(1L)).thenReturn(Optional.of(musculo));
        when(musculoRepository.existsByNombreIgnoreCaseAndIdNot("Dorsal ancho", 1L)).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> musculoService.updateMusculo(request, 1L));

        verify(musculoRepository, never()).save(any(Musculo.class));
    }

    @Test
    @DisplayName("Debería eliminar el músculo cuando no está asignado a ejercicios")
    void shouldDeleteMusculoSuccessful() {
        when(musculoRepository.existsById(1L)).thenReturn(true);
        when(ejercicioRepository.findAllByMusculoId(1L)).thenReturn(List.of());

        musculoService.deleteMusculos(1L);

        verify(musculoRepository, times(1)).deleteById(1L);
        verify(musculoRepository, times(1)).flush();
    }

    @Test
    @DisplayName("No debería eliminar un músculo que está asignado a ejercicios")
    void shouldNotDeleteMusculoUsedByEjercicios() {
        Ejercicio pressBanca = Ejercicio.builder().id(1L).nombre("Press banca").build();
        Ejercicio fondos = Ejercicio.builder().id(2L).nombre("Fondos").build();

        when(musculoRepository.existsById(1L)).thenReturn(true);
        when(ejercicioRepository.findAllByMusculoId(1L)).thenReturn(List.of(pressBanca, fondos));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> musculoService.deleteMusculos(1L));

        assertTrue(exception.getMessage().contains("Press banca"));
        assertTrue(exception.getMessage().contains("Fondos"));
        verify(musculoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar un músculo inexistente")
    void shouldThrowWhenDeletingMusculoNotFound() {
        when(musculoRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> musculoService.deleteMusculos(1L));

        verify(ejercicioRepository, never()).findAllByMusculoId(anyLong());
        verify(musculoRepository, never()).deleteById(anyLong());
    }
}
