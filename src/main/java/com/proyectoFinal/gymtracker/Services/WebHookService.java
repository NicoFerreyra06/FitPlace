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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebHookService {

    private final SuscripcionGimnasioRepository suscripcionGimnasioRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${mp.webhook.secret}")
    private String webhookSecret;

    public ResponseEntity<Void> recibirWebhook(Map<String, Object> payload, String xSignature, String xRequestId) throws MPException {

        String type = (String) payload.get("type");

        if (!"payment".equals(type)) {
            return ResponseEntity.ok().build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        String paymentId = String.valueOf(data.get("id"));

        // Verificar la firma si está presente (evitar fallos si hay pagos de prueba antiguos, aunque idealmente debe fallar)
        if (xSignature != null && !xSignature.isEmpty() && xRequestId != null && !xRequestId.isEmpty()) {
            if (!validarFirma(xSignature, xRequestId, paymentId)) {
                System.out.println("Firma de Webhook de Mercado Pago inválida para paymentId: " + paymentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
             // Dependiendo de tu nivel de seguridad, podrías rechazar la petición si no hay firma
             // Para ser seguros, lo rechazamos
             System.out.println("Headers de firma ausentes para paymentId: " + paymentId);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            PaymentClient paymentClient = new PaymentClient();
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

    private boolean validarFirma(String xSignature, String xRequestId, String dataId) {
        try {
            String ts = "";
            String v1 = "";

            // Parsear el header x-signature (formato: ts=123456,v1=hash)
            String[] parts = xSignature.split(",");
            for (String part : parts) {
                if (part.trim().startsWith("ts=")) {
                    ts = part.trim().substring(3);
                } else if (part.trim().startsWith("v1=")) {
                    v1 = part.trim().substring(3);
                }
            }

            if (ts.isEmpty() || v1.isEmpty()) {
                return false;
            }

            // Construir el manifest
            String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

            // Generar HMAC-SHA256
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(manifest.getBytes());
            String hashGenerado = bytesToHex(hashBytes);

            // Comparar
            return hashGenerado.equals(v1);

        } catch (Exception e) {
            System.out.println("Error validando firma de Mercado Pago: " + e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
