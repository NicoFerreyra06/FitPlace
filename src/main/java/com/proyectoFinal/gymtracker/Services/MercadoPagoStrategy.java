package com.proyectoFinal.gymtracker.Services;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.proyectoFinal.gymtracker.Interfaces.IPagoService;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class MercadoPagoStrategy implements IPagoService {

    @Value("${mercadopago.notification}")
    private String urlNotification;

    @Value("${mercadopago.url.return}")
    private String urlReturn;

    @Override
    public String generarLinkPago(SuscripcionGimnasio suscripcion) {

        try {
            PreferenceClient client = new PreferenceClient();
            
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Suscripción gimnasio - " + suscripcion.getGimnasio().getNombre())
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(suscripcion.getCosto()))
                    .build();

            List<PreferenceItemRequest> items = List.of(item);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(urlReturn)
                    .failure(urlReturn)
                    .pending(urlReturn)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .notificationUrl(urlNotification)
                    .externalReference(suscripcion.getId().toString())
                    .marketplaceFee(BigDecimal.valueOf(suscripcion.getComisionApp()))
                    .build();

            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(suscripcion.getGimnasio().getMpAccessToken())
                    .build();

            Preference preference = client.create(request, requestOptions);

            return preference.getInitPoint();

        } catch (MPApiException e) {
            log.error("Error Mercado Pago al crear preferencia. Status: {}, Content: {}",
                    e.getApiResponse().getStatusCode(), e.getApiResponse().getContent());

            throw new RuntimeException(
                    "Error Mercado Pago: " + e.getApiResponse().getContent(), e
            );
        } catch (Exception e) {
            log.error("Error inesperado al crear preferencia de pago: {}", e.getMessage());
            throw new RuntimeException("Error Mercado Pago: " + e);
        }
    }
}
