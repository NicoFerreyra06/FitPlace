package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Request.MarcaEjercicioRequest;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.ForbiddenAccessException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.*;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.EntrenamientoLogRepository;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntrenamientoLogServiceTest {

    @Mock
    private RutinaRepository rutinaRepository;

    @Mock
    private EjercicioRutinaRepository ejercicioRutinaRepository;

    @Mock
    private EntrenamientoLogRepository entrenamientoLogRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecordPersonalService recordPersonalService;

    @InjectMocks
    private EntrenamientoLogService entrenamientoLogService;

    private EntrenamientoLogRequest entrenamientoLogRequest;

    private Usuario usuario;

    private Rutina rutina;

    private Ejercicio ejercicio;

    private MarcaEjercicio marcaEjercicio;

    private EntrenamientoLog logSimulado;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("nico_dev")
                .email("nico@test.com")
                .password("password")
                .rol(Rol.USUARIO)
                .peso(75.5)
                .altura(1.80)
                .codigoAmigo(UUID.randomUUID().toString())
                .rutinaActivaDesde(LocalDate.now())
                .rachaActualDias(3)
                .rachaMaximaDias(10)
                .build();

        MarcaEjercicioRequest marcaEjercicioRequest = MarcaEjercicioRequest.builder()
                .pesoLevantado(20.0)
                .repeticionesLogradas(5)
                .ejercicioRutinaId(1L).build();

        entrenamientoLogRequest = EntrenamientoLogRequest.builder()
                .idRutina(1L)
                .marcasEjercicio(List.of(marcaEjercicioRequest)).build();

        rutina = Rutina.builder()
                .id(1L)
                .creador(usuario)
                .nombre("Test_rutina")
                .tokenCompartir(UUID.randomUUID().toString())
                .dias(List.of()).build();


        ejercicio = Ejercicio.builder()
                .id(1L)
                .nombre("Ejercicio")
                .descripcion("descripcion")
                .musculosPrincipales(List.of())
                .musculosSecundarios(List.of()).build();

        marcaEjercicio = MarcaEjercicio.builder()
                .id(1L)
                .pesoLevantado(20.0)
                .repeticionesLogradas(5)
                .ejercicioRutina(crearEjercicioRutinaSimulado())
                .build();

        logSimulado = EntrenamientoLog.builder()
                .id(99L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .marcas(List.of(marcaEjercicio))
                .build();
    }

    @Test
    @DisplayName("Deberia crear correctamente un EntrenamientoLog")
    void shouldCreateEntrenamientoLog() {
        usuario.setRachaActualDias(0);

        EjercicioRutina ejercicioRutina = crearEjercicioRutinaSimulado();

        EntrenamientoLog logSimulado = EntrenamientoLog.builder()
                .id(99L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .marcas(List.of(MarcaEjercicio.builder()
                        .pesoLevantado(20.0)
                        .repeticionesLogradas(5)
                        .ejercicioRutina(ejercicioRutina)
                        .build()))
                .build();

        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.of(ejercicioRutina));
        when(entrenamientoLogRepository.save(any(EntrenamientoLog.class))).thenReturn(logSimulado);

        var response = entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario);

        assertNotNull(response);
        verify(rutinaRepository, times(1)).findById(1L);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(recordPersonalService, times(1)).recalcularRecord(eq(usuario), any(Ejercicio.class));
        verify(entrenamientoLogRepository, times(1)).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al registrar un entrenamiento con una rutina privada ajena")
    void shouldThrowExceptionWhenRutinaPrivadaAjenaOnAdd() {
        Usuario ajeno = crearUsuarioAjeno();

        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest, ajeno));

        assertEquals("No tienes permiso para utilizar esta rutina privada", exception.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion al editar un entrenamiento con una rutina privada ajena")
    void shouldThrowExceptionWhenRutinaPrivadaAjenaOnUpdate() {
        Usuario ajeno = crearUsuarioAjeno();

        EntrenamientoLog entrenamientoPropio = EntrenamientoLog.builder()
                .id(1L)
                .usuario(ajeno)
                .fecha(LocalDate.now())
                .build();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoPropio));
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));

        ForbiddenAccessException exception = assertThrows(ForbiddenAccessException.class,
                () -> entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, ajeno));

        assertEquals("No tienes permiso para utilizar esta rutina privada", exception.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    private Usuario crearUsuarioAjeno() {
        return Usuario.builder()
                .id(2L)
                .username("ajeno_dev")
                .email("ajeno@test.com")
                .password("password")
                .rol(Rol.USUARIO)
                .peso(75.5)
                .altura(1.80)
                .codigoAmigo(UUID.randomUUID().toString())
                .build();
    }
    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra la rutina")
    void shouldThrowExceptionWhenRutineNotFound() {
        when(rutinaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario));

        assertNotNull(exception);
        assertEquals("Rutina no encontrada", exception.getMessage());
        verify(rutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra el ejercicio")
    void shouldThrowExceptionWhenEjercicioNotFound() {
        usuario.setRachaActualDias(0);
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario));

        assertNotNull(exception);
        assertEquals("Ejercicio no encontrado", exception.getMessage());
        verify(rutinaRepository, times(1)).findById(1L);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia actualizar correctamente")
    void shouldUpdateSuccessfullyEntrenamientoLog() {
        EntrenamientoLog entrenamientoLogViejo = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        EntrenamientoLog entrenamientoLogUpdate = EntrenamientoLog.builder()
                .id(2L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        EjercicioRutina ejercicioRutina = crearEjercicioRutinaSimulado();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoLogViejo));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.of(ejercicioRutina));
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(entrenamientoLogRepository.save(any(EntrenamientoLog.class))).thenReturn(entrenamientoLogUpdate);

        var response = entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, usuario);

        assertNotNull(response);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, times(1)).findById(1L);
        verify(rutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, times(1)).save(any(EntrenamientoLog.class));
        assertEquals("Test_rutina", response.getNombreRutina());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra el entrenamiento")
    void shouldThrowExceptionWhenEntrenamientoLogNotFound() {
        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.empty());

        var response = assertThrows(ResourceNotFoundException.class,
                ()-> entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest,  1L, usuario));

        assertNotNull(response);
        assertEquals("Entrenamiento no encontrado", response.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque el log no le pertenece")
    void shouldThrowExceptionWhenUserItsDontCreator(){
        Usuario infiltrado = Usuario.builder()
                .id(2L)
                .username("infiltrado_dev")
                .email("infiltrado@test.com")
                .password("password")
                .rol(Rol.USUARIO)
                .peso(75.5)
                .altura(1.80)
                .codigoAmigo(UUID.randomUUID().toString())
                .rutinaActivaDesde(LocalDate.now())
                .rachaActualDias(3)
                .rachaMaximaDias(10)
                .build();

        EntrenamientoLog entrenamientoLogViejo = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoLogViejo));

        var response = assertThrows(ForbiddenAccessException.class,
                ()-> entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, infiltrado));

        assertNotNull(response);
        assertEquals("No tenés permiso para editar un entrenamiento que no es tuyo", response.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia recalcular la racha actual al borrar un entrenamiento y conservar la maxima")
    void shouldRecalculateRachaWhenDeletingEntrenamiento() {
        usuario.setRachaActualDias(3);
        usuario.setRachaMaximaDias(3);

        EntrenamientoLog entrenamientoBorrado = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.of(2026, 10, 6))
                .build();

        // Quedan el lunes y el miercoles: el martes (borrado) era dia de entrenamiento, asi que se rompe la racha
        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoBorrado));
        when(entrenamientoLogRepository.findFechasByUsuarioId(1L))
                .thenReturn(List.of(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 7)));

        entrenamientoLogService.deleteEntrenamientoLog(1L, usuario);

        verify(entrenamientoLogRepository, times(1)).delete(entrenamientoBorrado);
        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(1, usuario.getRachaActualDias());
        assertEquals(3, usuario.getRachaMaximaDias());
    }

    @Test
    @DisplayName("Deberia dejar la racha actual en 0 al borrar el unico entrenamiento")
    void shouldResetRachaWhenDeletingLastEntrenamiento() {
        usuario.setRachaActualDias(1);
        usuario.setRachaMaximaDias(5);

        EntrenamientoLog entrenamientoBorrado = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.of(2026, 10, 6))
                .build();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoBorrado));
        when(entrenamientoLogRepository.findFechasByUsuarioId(1L)).thenReturn(List.of());

        entrenamientoLogService.deleteEntrenamientoLog(1L, usuario);

        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(0, usuario.getRachaActualDias());
        assertEquals(5, usuario.getRachaMaximaDias());
    }

    @Test
    @DisplayName("Deberia recalcular la racha segun el historial al editar un entrenamiento")
    void shouldRecalculateRachaWhenUpdatingEntrenamiento() {
        usuario.setRachaActualDias(0);
        usuario.setRachaMaximaDias(0);

        EntrenamientoLog entrenamientoViejo = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoViejo));
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.of(crearEjercicioRutinaSimulado()));
        when(entrenamientoLogRepository.save(any(EntrenamientoLog.class))).thenReturn(entrenamientoViejo);
        when(entrenamientoLogRepository.findFechasByUsuarioId(1L)).thenReturn(List.of(
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 7)));

        entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, usuario);

        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(3, usuario.getRachaActualDias());
        assertEquals(3, usuario.getRachaMaximaDias());
    }

    private EjercicioRutina crearEjercicioRutinaSimulado() {
        DiaRutina diaRutina = DiaRutina
                .builder()
                .id(1L)
                .diaDeLaSemana(DayOfWeek.MONDAY)
                .rutina(rutina)
                .ejercicios(List.of()).build();

        return EjercicioRutina.builder()
                .id(1L)
                .dia(diaRutina)
                .ejercicio(ejercicio)
                .build();
    }
}