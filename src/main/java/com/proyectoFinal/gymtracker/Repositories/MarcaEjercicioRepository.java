package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.MarcaEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarcaEjercicioRepository extends JpaRepository<MarcaEjercicio,Long> {
    Optional<MarcaEjercicio> findFirstByEntrenamientoLog_Usuario_IdAndEjercicioRutina_Ejercicio_IdOrderByPesoLevantadoDesc(Long usuarioId, Long ejercicioId);
}
