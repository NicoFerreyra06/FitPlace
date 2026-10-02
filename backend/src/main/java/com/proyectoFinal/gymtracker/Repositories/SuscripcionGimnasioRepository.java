package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.proyectoFinal.gymtracker.DTO.Response.GananciaGimnasioProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SuscripcionGimnasioRepository extends JpaRepository<SuscripcionGimnasio, Long> {

    boolean existsByUsuarioAndEstadoSuscripcionIn(
            Usuario usuario,
            List<EstadoSuscripcion> estados
    );

    Optional<SuscripcionGimnasio> findByUsuarioAndEstadoSuscripcion(Usuario usuario, EstadoSuscripcion estadoSuscripcion);

    Optional<SuscripcionGimnasio> findByIdAndUsuario(Long id, Usuario usuario);

    List<SuscripcionGimnasio> findByEstadoSuscripcionAndFechaFinBefore(EstadoSuscripcion estadoSuscripcion, LocalDate hoy);

    Optional<SuscripcionGimnasio> findFirstByUsuarioAndEstadoSuscripcionIn(Usuario usuario, List<EstadoSuscripcion> estados);
    
    List<SuscripcionGimnasio> findByGimnasioId(Long gimnasioId);
    
    List<SuscripcionGimnasio> findByGimnasioIdAndEstadoSuscripcion(Long gimnasioId, EstadoSuscripcion estadoSuscripcion);

    @Query(
        value = """
            SELECT g.nombre AS nombreGimnasio,
                   SUM(s.costo) AS totalIngresosBrutos,
                   SUM(s.comision_app) AS comisionFitPlace,
                   SUM(s.costo - s.comision_app) AS gananciaNetaGimnasio
            FROM suscripcion_gimnasio s
            JOIN gimnasio g ON s.id_gimnasio = g.id
            WHERE (:desde IS NULL OR s.fecha_inicio >= :desde)
              AND (:hasta IS NULL OR s.fecha_inicio <= :hasta)
            GROUP BY g.id, g.nombre
            """,
        nativeQuery = true
    )
    List<GananciaGimnasioProjection> reporteGananciasPorGimnasio(
            @Param("desde") LocalDate desde, 
            @Param("hasta") LocalDate hasta
    );
}
