package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Config.JwtService;
import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.PerfilUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.LoginResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import com.proyectoFinal.gymtracker.Enum.Rol;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public UsuarioResponse registrar(UsuarioRequest usuarioRequest) {

        if (usuarioRequest.getEmail() == null || usuarioRequest.getEmail().isEmpty()) {
            throw new BusinessLogicException("Email obligatorio");
        }

        if (usuarioRepository.findByEmail(usuarioRequest.getEmail()).isPresent()) {
            throw new BusinessLogicException("Email ya registrado");
        }

        if (usuarioRepository.findByUsername(usuarioRequest.getUsername()).isPresent()) {
            throw new BusinessLogicException("Username ya existe");
        }

        Usuario usuario = Usuario.builder()
                .username(usuarioRequest.getUsername())
                .email(usuarioRequest.getEmail())
                .password(passwordEncoder.encode(usuarioRequest.getPassword()))
                .peso(usuarioRequest.getPeso())
                .altura(usuarioRequest.getAltura())
                .rol(Rol.USUARIO)
                .codigoAmigo(UUID.randomUUID().toString())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        return toResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {

        // El AuthenticationManager busca el mail y comprueba que la password encriptada coincida
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Si la contraseña era correcta, fabricamos la "pulsera" (el token)
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generarToken(userDetails);
        return new LoginResponse(token);
    }

    public UsuarioResponse verPerfilPropio(Usuario usuario) {

        return toResponse(usuario);
    }

    public UsuarioResponse verPerfilOtroUsuario(Long idUsuario){
        Usuario usuario =  usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return toResponse(usuario);
    }

    public UsuarioResponse editarPerfil(Usuario usuario, PerfilUpdateRequest request) {

        usuario.setPeso(request.getPeso());
        usuario.setAltura(request.getAltura());

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse activarRutina(Usuario usuario, Long idRutina) {

        Rutina rutina = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        usuario.setRutinaActiva(rutina);
        usuario.setRutinaActivaDesde(LocalDate.now());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse agregarAmigo(Usuario usuarioLog, String codigoAmigo) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Usuario amigo = usuarioRepository.findByCodigoAmigo(codigoAmigo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe usuario con ese código"));

        if (usuario.getId().equals(amigo.getId()))
            throw new BusinessLogicException("No podés agregarte a vos mismo");
        if (usuario.getAmigos().contains(amigo))
            throw new BusinessLogicException("Ya son amigos");

        usuario.getAmigos().add(amigo);
        amigo.getAmigos().add(usuario);
        usuarioRepository.save(amigo);
        
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse eliminarAmigo(Long amigoId, Usuario usuarioLog) {

        Usuario usuario = usuarioRepository.findById(usuarioLog.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        usuario.getAmigos().removeIf(amigo -> amigo.getId().equals(amigoId));
        
        Usuario amigoToRemove = usuarioRepository.findById(amigoId).orElse(null);
        if (amigoToRemove != null) {
            amigoToRemove.getAmigos().removeIf(u -> u.getId().equals(usuario.getId()));
            usuarioRepository.save(amigoToRemove);
        }
        
        return toResponse(usuarioRepository.save(usuario));
    }

    public List<AmigoResponse> getAmigos(Usuario usuarioLog) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return usuario.getAmigos().stream().map(this::toAmigoResponse).toList();
    }

    public UsuarioResponse verPerfilAmigo(Usuario usuarioLog, Long amigoId) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Usuario amigo = usuarioRepository.findById(amigoId)
                .orElseThrow(() -> new UserNotFoundException("Amigo no encontrado"));

        //no son amigos
        if (!usuario.getAmigos().contains(amigo)) {
            throw new BusinessLogicException("No tenés acceso al perfil de este usuario");
        }

        return toResponse(amigo);
    }

    @Transactional
    public UsuarioResponse asignarEntrenador(Long entrenadorId, Usuario usuario){
        Usuario entrenador = usuarioRepository.findById(entrenadorId)
                .orElseThrow(() -> new UserNotFoundException("Entrenador no encontrado"));

        if (!entrenador.getRol().equals(Rol.ENTRENADOR))
            throw new BusinessLogicException("El usuario seleccionado no es un entrenador");

        if (entrenadorId.equals(usuario.getId())) throw new BusinessLogicException("No se puede asignar a si mismo como entrenador");

        usuario.setEntrenador(entrenador);
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse eliminarEntrenador(Long usuarioId){
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        usuario.setEntrenador(null);
        return toResponse(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponse> getAlumnos(Long entrenadorId){
        return usuarioRepository.findByEntrenadorId(entrenadorId)
                .stream().map(this::toResponse).toList();
    }

    public UsuarioResponse verEntrenadorActual(Long idUsuario){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if(usuario.getEntrenador() == null) throw new BusinessLogicException("No tiene entrenador");

        return toResponse(usuario.getEntrenador());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> getUsuarios(Pageable pageable){
        return usuarioRepository.findAll(pageable).map(this::toResponse);
    }

    public List<UsuarioResponse> getEntrenadores() {
        return usuarioRepository.findByRol(Rol.ENTRENADOR).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarRol(Long idUsuario, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse asignarRutinaAAlumno(Usuario entrenador, Long idAlumno, Long idRutina) {
        Usuario alumno = usuarioRepository.findById(idAlumno)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        // Valida que el alumno realmente pertenezca a este entrenador
        if (alumno.getEntrenador() == null || !alumno.getEntrenador().getId().equals(entrenador.getId())) {
            throw new BusinessLogicException("No tienes permiso para asignarle rutinas a este alumno");
        }

        Rutina rutina = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        alumno.setRutinaActiva(rutina);
        alumno.setRutinaActivaDesde(LocalDate.now());
        return toResponse(usuarioRepository.save(alumno));
    }

    protected UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .codigoAmigo(usuario.getCodigoAmigo())
                .peso(usuario.getPeso())
                .altura(usuario.getAltura())
                .imc(usuario.getImc())
                .categoriaImc(calcularCategoriaImc(usuario.getImc()))
                .rachaActualDias(usuario.getRachaActualDias())
                .rachaMaximaDias(usuario.getRachaMaximaDias()).build();
    }

    private String calcularCategoriaImc(Double imc) {
        if (imc == null || imc <= 0) return null;
        if (imc < 18.5) return "Bajo peso";
        if (imc >= 18.5 && imc < 25.0) return "Peso Normal";
        if (imc >= 25.0 && imc < 30.0) return "Exceso de peso";
        if (imc >= 30.0 && imc < 35.0 ) return "Obesidad grado 1";
        if (imc >= 35.0 && imc < 40.0) return "Obesidad grado 2";
        return "Obesidad grado 3";
    }

    private AmigoResponse toAmigoResponse(Usuario usuario) {
        return AmigoResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .rachaActualDias(usuario.getRachaActualDias())
                .build();
    }
}