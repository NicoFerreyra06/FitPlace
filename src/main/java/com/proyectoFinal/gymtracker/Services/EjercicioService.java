package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioResponse;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;
    private final MusculoRepository musculoRepository;

    public EjercicioResponse addEjercicio(EjercicioRequest request) {
        Ejercicio ejercicio = Ejercicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .musculosPrincipales(musculoRepository.findAllById(request.getMusculoPrincipalId()))
                .musculosSecundarios(musculoRepository.findAllById(request.getMusculoSecundarioId()))
                .build();

        Ejercicio saved = ejercicioRepository.save(ejercicio);
        return toResponse(saved);
    }

    public List<EjercicioResponse> addEjercicios(List<EjercicioRequest> requests) {
        List<Ejercicio> ejercicios = requests.stream()
                .map(request -> Ejercicio.builder()
                        .nombre(request.getNombre())
                        .descripcion(request.getDescripcion())
                        .musculosPrincipales(musculoRepository.findAllById(request.getMusculoPrincipalId()))
                        .musculosSecundarios(musculoRepository.findAllById(request.getMusculoSecundarioId()))
                        .build()
                ).toList();

        return ejercicioRepository.saveAll(ejercicios).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EjercicioResponse updateEjercicio(EjercicioRequest ejercicioRequest, Long idEjercicio) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        ejercicio.setNombre(ejercicioRequest.getNombre());
        ejercicio.setDescripcion(ejercicioRequest.getDescripcion());
        ejercicio.setMusculosPrincipales(musculoRepository.findAllById(ejercicioRequest.getMusculoPrincipalId()));
        ejercicio.setMusculosSecundarios(musculoRepository.findAllById(ejercicioRequest.getMusculoSecundarioId()));

        return toResponse(ejercicioRepository.save(ejercicio));
    }

    public EjercicioResponse getById(Long id) {
        Ejercicio saved = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        return toResponse(saved);
    }

    public Page<EjercicioResponse> getAll(Pageable pageable) {
        return ejercicioRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public void deleteEjercicio(Long id) {
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        ejercicioRepository.delete(ejercicio);
    }

    public List<EjercicioResponse> getByMusculo(Long musculoId) {
        return ejercicioRepository.findByMusculo(musculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EjercicioResponse> getByMusculoPrincipal(Long musculoId) {
        return ejercicioRepository.findByMusculoPrincipal(musculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EjercicioResponse> buscarPorNombre(String nombre) {
        return ejercicioRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EjercicioResponse toResponse(Ejercicio ejercicio) {
        return EjercicioResponse.builder()
                .id(ejercicio.getId())
                .nombre(ejercicio.getNombre())
                .descripcion(ejercicio.getDescripcion())
                .musculosPrincipales(ejercicio.getMusculosPrincipales())
                .musculosSecundarios(ejercicio.getMusculosSecundarios())
                .build();
    }
}