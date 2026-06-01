package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse;
import com.proyectoFinal.gymtracker.Modelo.EntrenamientoLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntrenamientoLogRepository extends JpaRepository<EntrenamientoLog,Long> {
    Page<EntrenamientoLog> findByUsuarioId(Long idUsuario,  Pageable pageable);

    @Query("SELECT new com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse(el.fecha, MAX(mj.pesoLevantado)) " +
            "FROM EntrenamientoLog el " +
            "JOIN el.marcas mj " +
            "WHERE el.usuario.id = :usuarioId AND mj.ejercicioRutina.ejercicio.id = :idEjercicio " +
            "GROUP BY el.fecha ORDER BY el.fecha ASC")
    List<HistorialEjercicioResponse> historialEjercicio(
            @Param("usuarioId") long usuarioId,
            @Param("idEjercicio") long idEjercicio
    );
}
