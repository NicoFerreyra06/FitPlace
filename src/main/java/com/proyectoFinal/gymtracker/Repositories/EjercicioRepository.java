package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioRepository extends JpaRepository<Ejercicio,Long> {


    //esto para buscar los ejercicios poniendo el id del músculo principal en el endpoint
    @Query("SELECT e FROM Ejercicio e JOIN e.musculosPrincipales m WHERE m.id = :musculoId")
    List<Ejercicio> findByMusculoPrincipal(@Param("musculoId") Long musculoId);

    //y este para que busque el musculo de todas formas, sea el principal o el secundario
    @Query("SELECT DISTINCT e FROM Ejercicio e " +
            "LEFT JOIN e.musculosPrincipales mp " +
            "LEFT JOIN e.musculosSecundarios ms " +
            "WHERE mp.id = :musculoId OR ms.id = :musculoId")
    List<Ejercicio> findByMusculo(@Param("musculoId") Long musculoId);


    List<Ejercicio> findByNombreContainingIgnoreCase(String nombre);


}
