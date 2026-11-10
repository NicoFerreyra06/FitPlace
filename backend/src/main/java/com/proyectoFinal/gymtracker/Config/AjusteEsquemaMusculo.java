package com.proyectoFinal.gymtracker.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hibernate crea las columnas @Enumerated(STRING) como ENUM nativo de MySQL, con los valores que tenía
 * el enum al crear la tabla, y ddl-auto=update nunca la vuelve a modificar. En esas bases, guardar un
 * grupo muscular nuevo (por ejemplo GLUTEOS) fallaría. Este ajuste pasa musculo.grupo_muscular a VARCHAR
 * una sola vez; si la columna ya es VARCHAR no hace nada. Corre antes de la precarga del catálogo.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class AjusteEsquemaMusculo implements ApplicationRunner {

    static final String CONSULTA_TIPO =
            "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'musculo' AND COLUMN_NAME = 'grupo_muscular'";

    static final String CONVERTIR_A_VARCHAR =
            "ALTER TABLE musculo MODIFY COLUMN grupo_muscular VARCHAR(30) NOT NULL";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> tipo = jdbcTemplate.queryForList(CONSULTA_TIPO, String.class);
            if (!tipo.isEmpty() && "enum".equalsIgnoreCase(tipo.get(0))) {
                jdbcTemplate.execute(CONVERTIR_A_VARCHAR);
                log.info("Columna musculo.grupo_muscular convertida de ENUM a VARCHAR(30) para admitir grupos musculares nuevos");
            }
        } catch (DataAccessException e) {
            log.error("No se pudo revisar o ajustar la columna musculo.grupo_muscular", e);
        }

        try {
            jdbcTemplate.execute("UPDATE ejercicio SET activo = TRUE WHERE activo IS NULL");
        } catch (DataAccessException ignored) {
        }
    }
}
