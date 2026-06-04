package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.SuscripcionGimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class SuscripcionGimnasioService {

    private final SuscripcionGimnasioRepository suscripcionGimnasioRepository;
    private final UsuarioRepository usuarioRepository;
    private final GimnasioRepository gimnasioRepository;

    private static final double PORCENTAJE_COMISION = 0.10;


    public SuscripcionGimnasio crearSuscripcionGimnasio(Long idUsuario, Long idGimnasio) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Gimnasio gimnasio = gimnasioRepository.findById(idGimnasio)
                .orElseThrow(() -> new RuntimeException("Gimnasio no encontrado"));

        double costo = gimnasio.getPrecioCuota();
        double comision = costo * PORCENTAJE_COMISION;

        SuscripcionGimnasio suscripcionGimnasio = SuscripcionGimnasio.builder()
                .gimnasio(gimnasio).usuario(usuario).fechaInicio(LocalDate.now())
                .costo(costo)
                .estadoSuscripcion(EstadoSuscripcion.ACTIVA)
                .fechaFin(LocalDate.now().plusMonths(1))
                .comisionApp(comision)
                .build();

        return suscripcionGimnasioRepository.save(suscripcionGimnasio);
    }




}
