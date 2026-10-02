package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.DiaRutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

@Repository
public interface DiaRutinaRepository extends JpaRepository<DiaRutina,Long> {

    Optional<DiaRutina> findByRutinaIdAndDiaDeLaSemana(Long rutinaId, DayOfWeek diaDeLaSemana);

}
