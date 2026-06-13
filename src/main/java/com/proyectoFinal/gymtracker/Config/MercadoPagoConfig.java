package com.proyectoFinal.gymtracker.Config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfig {


    @Value("${mp.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);
    }
}
