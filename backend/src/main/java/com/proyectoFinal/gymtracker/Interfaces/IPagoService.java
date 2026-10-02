package com.proyectoFinal.gymtracker.Interfaces;

import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;

public interface IPagoService {

    String generarLinkPago(SuscripcionGimnasio suscripcion);
}
