package com.proyectoFinal.gymtracker.Services;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Enum.MetodoPago;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.SuscripcionGimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebHookService {

    private final SuscripcionGimnasioRepository suscripcionGimnasioRepository;
    private final UsuarioRepository usuarioRepository;

    public ResponseEntity<Void> recibirWebhook(@RequestBody Map<String, Object> payload) throws MPException {

        String type = (String) payload.get("type");

        if (!"payment".equals(type)) {
            return ResponseEntity.ok().build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");

        String paymentId =
                String.valueOf(data.get("id"));
        try {

            PaymentClient paymentClient =
                    new PaymentClient();

            Payment payment = paymentClient.get(Long.parseLong(paymentId));

            if ("approved".equals(payment.getStatus())) {
                Long idSuscripcion = Long.parseLong(payment.getExternalReference());

                SuscripcionGimnasio suscripcion = suscripcionGimnasioRepository
                        .findById(idSuscripcion).orElseThrow(() -> new BusinessLogicException("Suscripcion no encontrada"));

                if (suscripcion.getEstadoSuscripcion() == EstadoSuscripcion.PENDIENTE) {

                    suscripcion.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
                    suscripcion.setFechaInicio(LocalDate.now());
                    suscripcion.setFechaFin(LocalDate.now().plusMonths(1));
                    suscripcion.setMetodoPago(MetodoPago.MERCADO_PAGO);

                    Usuario usuario = suscripcion.getUsuario();

                    usuario.setGimnasio(suscripcion.getGimnasio());

                    usuarioRepository.save(usuario);
                    suscripcionGimnasioRepository.save(suscripcion);
                }
            }

        } catch (MPApiException e) {

            System.out.println("Pago de prueba o inexistente: " + paymentId);
        }
        return ResponseEntity.ok().build();
    }
}
