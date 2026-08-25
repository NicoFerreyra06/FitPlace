package com.proyectoFinal.gymtracker.Services;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.proyectoFinal.gymtracker.Interfaces.IPagoService;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoService implements IPagoService {

    @Value("${mercadopago.notification}")
    private String urlNotification;

    @Value("${mercadopago.url.return}")
    private String urlReturn;

    @Override
    public String generarLinkPago(SuscripcionGimnasio suscripcion) {

        try {
            PreferenceClient client = new PreferenceClient();
            // 1. qué estás vendiendo
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Suscripción gimnasio - " + suscripcion.getGimnasio().getNombre())
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(suscripcion.getCosto()))
                    .build();

            List<PreferenceItemRequest> items = List.of(item);

            // 2. a dónde vuelve el usuario (opcional por ahora)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(urlReturn)
                    .failure(urlReturn)
                    .pending(urlReturn)
                    .build();

            // 3. armar la preferencia
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .notificationUrl(urlNotification)
                    .externalReference(suscripcion.getId().toString())
                    .build();

            // 4. crear en Mercado Pago
            Preference preference = client.create(request);

            // 5. esto es el link de pago
            return preference.getInitPoint();

        } catch (MPApiException e) {
            System.out.println("STATUS: " + e.getApiResponse().getStatusCode());
            System.out.println("CONTENT: " + e.getApiResponse().getContent());

            throw new RuntimeException(
                    "Error Mercado Pago: " + e.getApiResponse().getContent(), e
            );
        }catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error Mercado Pago: " + e);
        }
    }
}
