package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.GimnasioRequest;
import com.proyectoFinal.gymtracker.DTO.Request.GimnasioUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Response.GimnasioResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GimnasioService {

    private final GimnasioRepository gimnasioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public GimnasioResponse createGimnasio(GimnasioRequest request){

        Usuario adminGimnasio = usuarioRepository.findById(request.getAdminId())
                .orElseThrow(()-> new UserNotFoundException("Usuario no encontrado"));

        adminGimnasio.setRol(Rol.ADMIN_GIMNASIO);
        usuarioRepository.save(adminGimnasio);

         Gimnasio gimnasio = Gimnasio.builder()
                 .nombre(request.getNombre())
                 .direccion(request.getDireccion())
                 .precioCuota(request.getPrecioCuota())
                 .admin(adminGimnasio)
                 .horarioApertura(request.getHorarioApertura())
                 .horarioCierre(request.getHorarioCierre())
                 .diasAbierto(request.getDiasAbierto())
                 .build();

         return gimnasioToResponse(gimnasioRepository.save(gimnasio));
    }

    public List<GimnasioResponse> getPropioGimnasios(){
        Usuario admin = (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        List<Gimnasio> gimnasios = gimnasioRepository.findByAdminId(admin.getId());

        return gimnasios.stream().map(this::gimnasioToResponse).toList();
    }

    public Page<GimnasioResponse> getAllGimnasios(Pageable pageable){
        return gimnasioRepository.findAll(pageable).map(this::gimnasioToResponse);
    }

    @Transactional
    public GimnasioResponse asignarGimnasio(Long idGimnasio){
        Gimnasio gimnasio =  gimnasioRepository.findById(idGimnasio)
                .orElseThrow(() -> new BusinessLogicException("Gimnasio no encontrado"));

        Usuario principal = (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Usuario usuario = usuarioRepository.findById(principal.getId())
                        .orElseThrow(() -> new BusinessLogicException("Usuario no encontrado"));

        usuario.setGimnasio(gimnasio);
        usuarioRepository.save(usuario);

        gimnasio.getMiembros().add(usuario);

        return gimnasioToResponse(gimnasio);
    }

    @Transactional
    public GimnasioResponse actualizarGimnasio(GimnasioUpdateRequest request){

        Usuario admin =  (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Gimnasio gimnasio = gimnasioRepository.findByAdminId(admin.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("El usuario no tiene gimnasios"));

        gimnasio.setNombre(request.getNombre());
        gimnasio.setDireccion(request.getDireccion());
        gimnasio.setPrecioCuota(request.getPrecioCuota());
        gimnasio.setHorarioApertura(request.getHorarioApertura());
        gimnasio.setHorarioCierre(request.getHorarioCierre());
        gimnasio.setDiasAbierto(request.getDiasAbierto());

        return gimnasioToResponse(gimnasioRepository.save(gimnasio));
    }

    private GimnasioResponse gimnasioToResponse(Gimnasio gimnasio){
        return GimnasioResponse.builder()
                .id(gimnasio.getId())
                .nombre(gimnasio.getNombre())
                .direccion(gimnasio.getDireccion())
                .precioCuota(gimnasio.getPrecioCuota())
                .idAdmin(gimnasio.getAdmin().getId())
                .nombreAdmin(gimnasio.getAdmin().getUsername())
                .horarioApertura(gimnasio.getHorarioApertura())
                .horarioCierre(gimnasio.getHorarioCierre())
                .diasDeApertura(gimnasio.getDiasAbierto())
                .build();
    }
}
