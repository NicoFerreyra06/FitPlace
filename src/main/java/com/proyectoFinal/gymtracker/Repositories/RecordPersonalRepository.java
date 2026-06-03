package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordPersonalRepository extends JpaRepository<RecordPersonal, Long> {
    RecordPersonal findByUsuarioIdAndEjercicioId(Long usuarioId, Long ejercicioId);

    List<RecordPersonal> findRecordPersonalByUsuarioId(Long usuarioId);

    Page<RecordPersonal> findByEjercicioIdOrderByPesoMaximoDesc(Pageable pageable, Long ejercicioId);
}
