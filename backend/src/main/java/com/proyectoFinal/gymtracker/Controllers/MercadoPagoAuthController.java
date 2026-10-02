package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoAuthController {

    private final GimnasioRepository gimnasioRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mp.client.id:}")
    private String clientId;

    @Value("${mp.client.secret:}")
    private String clientSecret;

    @Value("${mp.redirect.uri:}")
    private String redirectUri;

    @GetMapping("/vincular/{idGimnasio}")
    @PreAuthorize("hasRole('ADMIN_GIMNASIO') or hasRole('ADMIN')")
    public ResponseEntity<String> getVinculacionUrl(@PathVariable Long idGimnasio) {
        String url = String.format("https://auth.mercadopago.com/authorization?client_id=%s&response_type=code&platform_id=mp&state=%s&redirect_uri=%s", 
                clientId, idGimnasio, redirectUri);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<String> mercadopagoCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        try {
            Long idGimnasio = Long.parseLong(state);
            Gimnasio gimnasio = gimnasioRepository.findById(idGimnasio)
                    .orElseThrow(() -> new RuntimeException("Gimnasio no encontrado"));

            String tokenUrl = "https://api.mercadopago.com/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_secret", clientSecret);
            map.add("client_id", clientId);
            map.add("grant_type", "authorization_code");
            map.add("code", code);
            map.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            Map response = restTemplate.postForObject(tokenUrl, request, Map.class);
            if (response != null && response.containsKey("access_token")) {
                gimnasio.setMpAccessToken((String) response.get("access_token"));
                if (response.containsKey("user_id")) {
                    gimnasio.setMpUserId(String.valueOf(response.get("user_id")));
                }
                gimnasioRepository.save(gimnasio);
                return ResponseEntity.ok("Vinculación exitosa. Puedes cerrar esta ventana.");
            } else {
                return ResponseEntity.badRequest().body("Error al vincular con Mercado Pago");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
