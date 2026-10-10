package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ForbiddenAccessException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MercadoPagoAuthService {

    private final GimnasioRepository gimnasioRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mp.client.id:}")
    private String clientId;

    @Value("${mp.client.secret:}")
    private String clientSecret;

    @Value("${mp.redirect.uri:}")
    private String redirectUri;

    public String getVinculacion(Long idGimnasio, Usuario usuario) {
        Gimnasio gimnasio = gimnasioRepository.findById(idGimnasio)
                .orElseThrow(() -> new ResourceNotFoundException("Gimnasio no encontrado"));

        if (usuario.getRol() != Rol.ADMIN && !gimnasio.getAdmin().getId().equals(usuario.getId())) {
            throw new ForbiddenAccessException("Usted no es el administrador de este gimnasio");
        }

        return String.format("https://auth.mercadopago.com/authorization?client_id=%s&response_type=code&platform_id=mp&state=%s&redirect_uri=%s",
                clientId, idGimnasio, redirectUri);
    }

    public String mercadoPagoCallback(String code, String state) {
        Gimnasio gimnasio = gimnasioRepository.findById(Long.parseLong(state))
                .orElseThrow(() -> new ResourceNotFoundException("Gimnasio no encontrado"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_secret", clientSecret);
        map.add("client_id", clientId);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectUri);

        Map response = restTemplate.postForObject("https://api.mercadopago.com/oauth/token", new HttpEntity<>(map, headers), Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new BusinessLogicException("Error al vincular con Mercado Pago");
        }

        gimnasio.setMpAccessToken((String) response.get("access_token"));
        if (response.containsKey("user_id")) {
            gimnasio.setMpUserId(String.valueOf(response.get("user_id")));
        }
        gimnasioRepository.save(gimnasio);

        return "Vinculación exitosa. Puedes cerrar esta ventana.";
    }
}
