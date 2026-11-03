package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Response.RecordPersonalResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.MarcaEjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.RecordPersonalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecordPersonalServiceTest {

    @Mock
    private RecordPersonalRepository recordPersonalRepository;

    @Mock
    private MarcaEjercicioRepository marcaEjercicioRepository;

    @InjectMocks
    private RecordPersonalService recordPersonalService;

    private Ejercicio pressBanca;
    private RecordPersonal recordNico;
    private RecordPersonal recordLucio;

    @BeforeEach
    public void setUp() {
        pressBanca = Ejercicio.builder()
                .id(1L)
                .nombre("Press banca")
                .build();

        Usuario nico = Usuario.builder()
                .id(1L)
                .username("nico_dev")
                .email("nico@test.com")
                .rol(Rol.USUARIO)
                .build();

        Usuario lucio = Usuario.builder()
                .id(2L)
                .username("lucio_dev")
                .email("lucio@test.com")
                .rol(Rol.USUARIO)
                .build();

        recordNico = RecordPersonal.builder()
                .id(10L)
                .usuario(nico)
                .ejercicio(pressBanca)
                .pesoMaximo(100.0)
                .fechaLogro(LocalDate.of(2026, 10, 1))
                .build();

        recordLucio = RecordPersonal.builder()
                .id(11L)
                .usuario(lucio)
                .ejercicio(pressBanca)
                .pesoMaximo(90.0)
                .fechaLogro(LocalDate.of(2026, 10, 5))
                .build();
    }

    @Test
    @DisplayName("Deberia devolver el ranking en el orden del repositorio con los datos de cada usuario")
    void rankingMapsRecordsInOrder() {
        Pageable pageable = PageRequest.of(0, 10);
        when(recordPersonalRepository.findRankingByEjercicioId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(recordNico, recordLucio), pageable, 2));

        Page<RecordPersonalResponse> ranking =
                recordPersonalService.getRankingRecordsPersonalesByEjercicioId(pageable, 1L);

        assertEquals(2, ranking.getTotalElements());
        RecordPersonalResponse primero = ranking.getContent().get(0);
        assertEquals("nico_dev", primero.getNombreUsuario());
        assertEquals(100.0, primero.getPesoMaximo());
        assertEquals(1L, primero.getEjercicioId());
        assertEquals("Press banca", primero.getNombreEjercicio());
        assertEquals(LocalDate.of(2026, 10, 1), primero.getFechaLogro());
        assertEquals("lucio_dev", ranking.getContent().get(1).getNombreUsuario());
        verify(recordPersonalRepository).findRankingByEjercicioId(1L, pageable);
    }

    @Test
    @DisplayName("Deberia devolver una pagina vacia si nadie tiene record en el ejercicio")
    void rankingEmptyWhenNoRecords() {
        Pageable pageable = PageRequest.of(0, 10);
        when(recordPersonalRepository.findRankingByEjercicioId(99L, pageable))
                .thenReturn(Page.empty(pageable));

        Page<RecordPersonalResponse> ranking =
                recordPersonalService.getRankingRecordsPersonalesByEjercicioId(pageable, 99L);

        assertTrue(ranking.isEmpty());
        assertEquals(0, ranking.getTotalElements());
    }

    @Test
    @DisplayName("Deberia devolver el record propio de un ejercicio")
    void recordPropioFound() {
        when(recordPersonalRepository.findByUsuarioIdAndEjercicioId(1L, 1L)).thenReturn(recordNico);

        RecordPersonalResponse record = recordPersonalService.getRecordPersonalByEjercicioId(1L, 1L);

        assertEquals(100.0, record.getPesoMaximo());
        assertEquals("nico_dev", record.getNombreUsuario());
    }

    @Test
    @DisplayName("Deberia lanzar ResourceNotFoundException si el usuario no tiene record en el ejercicio")
    void recordPropioNotFound() {
        when(recordPersonalRepository.findByUsuarioIdAndEjercicioId(1L, 1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> recordPersonalService.getRecordPersonalByEjercicioId(1L, 1L));
    }
}
