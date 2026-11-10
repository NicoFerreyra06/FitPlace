package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.MusculoRequest;
import com.proyectoFinal.gymtracker.DTO.Response.MusculoResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MusculoService {

    private final MusculoRepository musculoRepository;
    private final EjercicioRepository ejercicioRepository;

    public MusculoResponse addMusculo(MusculoRequest request){
        String nombre = request.getNombre().trim();
        if (musculoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessLogicException("Ya existe un músculo con el nombre '" + nombre + "'");
        }

        Musculo musculo = requestToMusculo(request);
        return musculoToResponse(musculoRepository.save(musculo));
    }

    public List<MusculoResponse> addMusculos (List<MusculoRequest> musculosRequests){
        List<Musculo> musculosList = musculosRequests.stream().map(this::requestToMusculo).toList();
        return musculoRepository.saveAll(musculosList).stream().map(this::musculoToResponse).toList();
    }

    @Transactional
    public void deleteMusculos(Long idMusculo){
        if (!musculoRepository.existsById(idMusculo)) {
            throw new ResourceNotFoundException("Músculo no encontrado");
        }

        List<Ejercicio> ejerciciosEnUso = ejercicioRepository.findAllByMusculoId(idMusculo);
        if (!ejerciciosEnUso.isEmpty()) {
            String nombres = ejerciciosEnUso.stream()
                    .map(Ejercicio::getNombre)
                    .sorted()
                    .limit(5)
                    .collect(Collectors.joining(", "));
            int restantes = ejerciciosEnUso.size() - 5;
            String detalle = restantes > 0 ? nombres + " y " + restantes + " más" : nombres;
            throw new BusinessLogicException("No se puede eliminar el músculo porque está asignado a uno o más ejercicios: " + detalle);
        }

        try {
            musculoRepository.deleteById(idMusculo);
            musculoRepository.flush();
        } catch (DataIntegrityViolationException e){
            throw new BusinessLogicException("No se puede eliminar el músculo porque está asignado a uno o más ejercicios.");
        }
    }

    public MusculoResponse getMusculoById(Long idMusculo){
        Musculo musculo = musculoRepository.findById(idMusculo)
                .orElseThrow(() -> new ResourceNotFoundException("Músculo no encontrado"));

        return musculoToResponse(musculo);
    }

    public Page<MusculoResponse> getMusculos(Pageable pageable){
        return musculoRepository.findAll(pageable).map(this::musculoToResponse);
    }

    public MusculoResponse updateMusculo(MusculoRequest request, Long idMusculo){
        Musculo musculoExistente = musculoRepository.findById(idMusculo)
                .orElseThrow(() -> new ResourceNotFoundException("Músculo no encontrado"));

        String nombre = request.getNombre().trim();
        if (musculoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idMusculo)) {
            throw new BusinessLogicException("Ya existe otro músculo con el nombre '" + nombre + "'");
        }

        musculoExistente.setNombre(nombre);
        musculoExistente.setGrupoMuscular(request.getGrupoMuscular());
        musculoExistente.setFuncionAnatomica(request.getFuncionAnatomica() != null && !request.getFuncionAnatomica().isBlank()
                ? request.getFuncionAnatomica().trim() : null);

        return musculoToResponse(musculoRepository.save(musculoExistente));
    }

    public List<MusculoResponse> getByGrupoMuscular(GrupoMuscular grupoMuscular){
        return musculoRepository.findByGrupoMuscular(grupoMuscular)
                .stream().map(this::musculoToResponse).toList();
    }

    private Musculo requestToMusculo(MusculoRequest request){
        return Musculo.builder()
                .nombre(request.getNombre().trim())
                .grupoMuscular(request.getGrupoMuscular())
                .funcionAnatomica(request.getFuncionAnatomica() != null && !request.getFuncionAnatomica().isBlank()
                        ? request.getFuncionAnatomica().trim() : null)
                .build();
    }

    private MusculoResponse musculoToResponse(Musculo musculo){
        return MusculoResponse.builder()
                .id(musculo.getId())
                .nombre(musculo.getNombre())
                .grupoMuscular(musculo.getGrupoMuscular())
                .funcionAnatomica(musculo.getFuncionAnatomica())
                .build();
    }
}
