package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EjercicioServiceTest {

    @Mock
    private MusculoRepository musculoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private EjercicioService ejercicioService;

    private Ejercicio ejercicio;

    private EjercicioRequest ejercicioRequest;

    private Musculo musculo;

    @BeforeEach
    public void setUp() {
        ejercicioRequest = EjercicioRequest
                .builder()
                .nombre("ejercicio")
                .descripcion("Test")
                .musculoPrincipalId(List.of(1L))
                .musculoSecundarioId(List.of(1L))
                .build();

        musculo = Musculo.builder()
                .id(1L)
                .nombre("musculo")
                .grupoMuscular(GrupoMuscular.CUADRICEPS)
                .build();

        ejercicio = Ejercicio.builder()
                .id(1L)
                .nombre("ejercicio")
                .descripcion("Test")
                .musculosPrincipales(List.of(musculo))
                .musculosSecundarios(List.of(musculo)).build();


    }

    @Test
    void shouldCreateEjercicioSuccessful(){

        when(musculoRepository.findAllById(List.of(musculo.getId()))).thenReturn(List.of(musculo));
        when(ejercicioRepository.save(any(Ejercicio.class))).thenReturn(ejercicio);

        EjercicioResponse ejercicioResponse = ejercicioService.addEjercicio(ejercicioRequest);

        assertNotNull(ejercicioResponse);
        assertEquals(ejercicio.getNombre(), ejercicioResponse.getNombre());
        verify(ejercicioRepository, times(1)).save(any(Ejercicio.class));
    }
}
