package com.proyectoFinal.gymtracker.Config;

import com.proyectoFinal.gymtracker.DTO.Catalogo.CatalogoInicial;
import com.proyectoFinal.gymtracker.Services.CatalogoInicialService;
import com.proyectoFinal.gymtracker.Services.CatalogoInicialService.ResultadoCarga;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Al iniciar el back, carga los músculos y ejercicios de resources/catalogo/catalogo.json que falten.
 * Se desactiva con app.catalogo.precarga=false. Si el archivo no se puede leer, lo avisa en el log
 * y el back arranca igual.
 */
@Slf4j
@Component
public class CatalogoInicialRunner implements ApplicationRunner {

    public static final String RUTA_CATALOGO = "catalogo/catalogo.json";

    private final CatalogoInicialService catalogoInicialService;
    private final boolean habilitado;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public CatalogoInicialRunner(CatalogoInicialService catalogoInicialService,
                                 @Value("${app.catalogo.precarga:true}") boolean habilitado) {
        this.catalogoInicialService = catalogoInicialService;
        this.habilitado = habilitado;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado) return;

        try (InputStream json = new ClassPathResource(RUTA_CATALOGO).getInputStream()) {
            CatalogoInicial catalogo = objectMapper.readValue(json, CatalogoInicial.class);
            ResultadoCarga resultado = catalogoInicialService.cargar(catalogo);
            log.info("Catálogo inicial: {} músculos y {} ejercicios nuevos, {} ejercicios salteados",
                    resultado.musculosCreados(), resultado.ejerciciosCreados(), resultado.ejerciciosSalteados());
        } catch (IOException | JacksonException e) {
            log.error("No se pudo leer {}; el back arranca sin precargar el catálogo", RUTA_CATALOGO, e);
        }
    }
}
