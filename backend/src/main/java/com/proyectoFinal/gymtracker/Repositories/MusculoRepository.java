package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusculoRepository extends JpaRepository<Musculo, Long> {
    List<Musculo> findByGrupoMuscular(GrupoMuscular grupoMuscular);
}
