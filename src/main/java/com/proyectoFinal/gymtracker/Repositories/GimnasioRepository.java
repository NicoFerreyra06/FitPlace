package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.DTO.Request.GimnasioRequest;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GimnasioRepository extends JpaRepository<Gimnasio, Long> {
    List<Gimnasio> findByAdminId(Long id);
}
