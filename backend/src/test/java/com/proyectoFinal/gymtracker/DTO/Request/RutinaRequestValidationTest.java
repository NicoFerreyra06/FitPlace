package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("Deberia aceptar un dia sin nombre o con hasta 40 caracteres")
    void acceptsNombreDiaOptionalAndUpTo40() {
        assertTrue(validator.validate(rutinaConDia(null)).isEmpty());
        assertTrue(validator.validate(rutinaConDia("a".repeat(40))).isEmpty());
    }

    @Test
    @DisplayName("Deberia rechazar un nombre de dia de mas de 40 caracteres")
    void rejectsNombreDiaLongerThan40() {
        Set<ConstraintViolation<RutinaRequest>> errores = validator.validate(rutinaConDia("a".repeat(41)));

        assertEquals(1, errores.size());
        ConstraintViolation<RutinaRequest> error = errores.iterator().next();
        assertEquals("dias[0].nombre", error.getPropertyPath().toString());
        assertEquals("El nombre del día no puede superar los 40 caracteres", error.getMessage());
    }

    @Test
    @DisplayName("Deberia validar tambien los ejercicios de cada dia")
    void validatesEjerciciosInsideDias() {
        RutinaRequest request = rutinaConDia("Push A");
        request.getDias().get(0).setEjercicios(List.of());

        Set<ConstraintViolation<RutinaRequest>> errores = validator.validate(request);

        assertEquals(1, errores.size());
        assertEquals("dias[0].ejercicios", errores.iterator().next().getPropertyPath().toString());
    }
}
