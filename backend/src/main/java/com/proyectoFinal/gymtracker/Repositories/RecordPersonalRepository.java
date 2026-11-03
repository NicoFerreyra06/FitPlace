package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordPersonalRepository extends JpaRepository<RecordPersonal, Long> {
    RecordPersonal findByUsuarioIdAndEjercicioId(Long usuarioId, Long ejercicioId);

    List<RecordPersonal> findRecordPersonalByUsuarioId(Long usuarioId);

    // Ranking de un ejercicio. Usuario y ejercicio se traen en la misma consulta (evita el N+1)
    // y el desempate por fecha e id deja un orden estable para que la paginación no repita ni saltee filas.
    @EntityGraph(attributePaths = {"usuario", "ejercicio"})
    @Query("SELECT r FROM RecordPersonal r WHERE r.ejercicio.id = :ejercicioId " +
            "ORDER BY r.pesoMaximo DESC, r.fechaLogro ASC, r.id ASC")
    Page<RecordPersonal> findRankingByEjercicioId(@Param("ejercicioId") Long ejercicioId, Pageable pageable);

    Optional<RecordPersonal> findFirstByUsuarioIdOrderByFechaLogroDesc(Long usuarioId);
}
