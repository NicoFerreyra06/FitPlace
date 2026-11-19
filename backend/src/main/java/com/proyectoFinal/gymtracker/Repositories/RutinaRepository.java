package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina,Long> {

    Optional<Rutina> findByIdAndActivaTrue(Long id);

    Page<Rutina> findByEsPublicaTrueAndActivaTrue(Pageable pageable);
    
    Page<Rutina> findByCreadorIdAndActivaTrue(Long creadorId, Pageable pageable);

    @Query("SELECT r FROM Rutina r WHERE r.activa = true AND (" +
            " r.creador.id = :usuarioId" +
            " OR (r.esPublica = true AND r.creador.id = :entrenadorId)" +
            " OR r.id = :rutinaActivaId)")
    Page<Rutina> findDisponibles(@Param("usuarioId") Long usuarioId,
                                 @Param("entrenadorId") Long entrenadorId,
                                 @Param("rutinaActivaId") Long rutinaActivaId,
                                 Pageable pageable);
}
