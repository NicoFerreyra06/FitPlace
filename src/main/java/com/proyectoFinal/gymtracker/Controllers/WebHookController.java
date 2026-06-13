package com.proyectoFinal.gymtracker.Controllers;

import com.mercadopago.exceptions.MPException;
import com.proyectoFinal.gymtracker.Services.WebHookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook/mercadopago")
@RequiredArgsConstructor
public class WebHookController {

    private final WebHookService webHookService;

    @PostMapping
    public ResponseEntity<Void> recibirWebhook(
            @RequestBody Map<String, Object> payload) throws MPException {

        return webHookService.recibirWebhook(payload);
    }
}
