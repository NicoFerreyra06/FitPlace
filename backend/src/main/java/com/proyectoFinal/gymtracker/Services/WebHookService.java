package com.proyectoFinal.gymtracker.Services;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Enum.MetodoPago;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.SuscripcionGimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookService {

    private final SuscripcionGimnasioRepository suscripcionGimnasioRepository;
    private final UsuarioRepository usuarioRepository;
    private final GimnasioRepository gimnasioRepository;

    @Value("${mp.webhook.secret}")
    private String webhookSecret;

    public ResponseEntity<Void> recibirWebhook(Map<String, Object> payload, String xSignature, String xRequestId) throws MPException {

        String type = (String) payload.get("type");

        if (!"payment".equals(type)) {
            return ResponseEntity.ok().build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null || !data.containsKey("id")) {
            log.warn("El webhook no contiene la data del pago");
            return ResponseEntity.badRequest().build();
        }
        String paymentId = String.valueOf(data.get("id"));

        // Verificar la firma si está presente
        if (xSignature != null && !xSignature.isEmpty() && xRequestId != null && !xRequestId.isEmpty()) {
            if (!validarFirma(xSignature, xRequestId, paymentId)) {
                log.warn("Firma de Webhook de Mercado Pago inválida para paymentId: {}", paymentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
             log.warn("Headers de firma ausentes para paymentId: {}", paymentId);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Object sellerId = payload.get("user_id");
        if (sellerId == null) {
            log.warn("El webhook no trae user_id del vendedor. paymentId: {}", paymentId);
            return ResponseEntity.ok().build();
        }

        Optional<Gimnasio> gimnasioOpt = gimnasioRepository.findByMpUserId(String.valueOf(sellerId));
        if (gimnasioOpt.isEmpty() || gimnasioOpt.get().getMpAccessToken() == null) {
            log.warn("No hay gimnasio vinculado al user_id {} (paymentId: {})", sellerId, paymentId);
            return ResponseEntity.ok().build();
        }
        Gimnasio gimnasio = gimnasioOpt.get();

        try {
            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(gimnasio.getMpAccessToken())
                    .build();
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId), requestOptions);

            if ("approved".equals(payment.getStatus())) {
                Long idSuscripcion = Long.parseLong(payment.getExternalReference());

                SuscripcionGimnasio suscripcion = suscripcionGimnasioRepository
                        .findById(idSuscripcion).orElseThrow(() -> new BusinessLogicException("Suscripcion no encontrada"));

                if (!suscripcion.getGimnasio().getId().equals(gimnasio.getId())) {
                    log.warn("La suscripcion {} no pertenece al gimnasio {} del vendedor {}",
                            idSuscripcion, gimnasio.getId(), sellerId);
                    return ResponseEntity.ok().build();
                }

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
            log.info("Pago de prueba o inexistente: {}", paymentId);
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
            log.error("Error validando firma de Mercado Pago: {}", e.getMessage());
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
