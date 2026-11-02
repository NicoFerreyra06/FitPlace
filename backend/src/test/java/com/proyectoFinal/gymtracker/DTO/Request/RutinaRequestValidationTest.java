package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RutinaRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private RutinaRequest rutinaConDia(String nombreDia) {
        EjercicioRutinaRequest ejercicio = new EjercicioRutinaRequest();
        ejercicio.setEjercicioId(1L);
        ejercicio.setSeries(4);
        ejercicio.setRepeticiones(8);

        DiaRutinaRequest dia = new DiaRutinaRequest();
        dia.setDiaDeLaSemana(DayOfWeek.MONDAY);
        dia.setNombre(nombreDia);
        dia.setEjercicios(List.of(ejercicio));

        return RutinaRequest.builder()
                .nombre("Fuerza 4 dias")
                .esPublica(false)
                .dias(List.of(dia))
                .build();
    }

    private ConstraintViolation<RutinaRequest> unicoError(RutinaRequest request) {
        Set<ConstraintViolation<RutinaRequest>> errores = validator.validate(request);
        assertEquals(1, errores.size());
        return errores.iterator().next();
    }

    @Test
    @DisplayName("Deberia aceptar un nombre de dia de hasta 40 caracteres")
    void acceptsNombreDiaUpTo40() {
        assertTrue(validator.validate(rutinaConDia("Push A")).isEmpty());
        assertTrue(validator.validate(rutinaConDia("a".repeat(40))).isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deberia exigir el nombre del dia")
    void rejectsMissingNombreDia(String nombreDia) {
        ConstraintViolation<RutinaRequest> error = unicoError(rutinaConDia(nombreDia));

        assertEquals("dias[0].nombre", error.getPropertyPath().toString());
        assertEquals("Cada día de la rutina necesita un nombre", error.getMessage());
    }

    @Test
    @DisplayName("Deberia rechazar un nombre de dia de mas de 40 caracteres")
    void rejectsNombreDiaLongerThan40() {
        ConstraintViolation<RutinaRequest> error = unicoError(rutinaConDia("a".repeat(41)));

        assertEquals("dias[0].nombre", error.getPropertyPath().toString());
        assertEquals("El nombre del día no puede superar los 40 caracteres", error.getMessage());
    }

    @Test
    @DisplayName("Deberia validar tambien los ejercicios de cada dia")
    void validatesEjerciciosInsideDias() {
        RutinaRequest request = rutinaConDia("Push A");
        request.getDias().get(0).setEjercicios(List.of());

        assertEquals("dias[0].ejercicios", unicoError(request).getPropertyPath().toString());
    }
}
