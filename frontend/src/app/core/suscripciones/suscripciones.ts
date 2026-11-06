import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SuscripcionGimnasioResponse {
    id: number,
    idGimnasio: number,
    nombreGimnasio: string,
    idUsuario: number, 
    nombreUsuario: string,
    fechaInicio: string | null,
    fechaFin: string | null,
    metodoPago: string | null,
    estadoSuscripcion: estadoSuscripcion,
}

type estadoSuscripcion = 'ACTIVA' | 'PENDIENTE' | 'VENCIDA' | 'CANCELADA'

@Service()
export class Suscripciones {
    private readonly http = inject(HttpClient);
    private readonly API_URL_SUSCRIPCION = 'http://localhost:8080/suscripciones';
    private readonly API_URL_PAGOS = 'http://localhost:8080/pagos';

    getSuscripcion(): Observable<SuscripcionGimnasioResponse>{
        return this.http.get<SuscripcionGimnasioResponse>(`${this.API_URL_SUSCRIPCION}/mia`)
    }

    createSuscripcion(idGimnasio: number): Observable<SuscripcionGimnasioResponse>{
        return this.http.post<SuscripcionGimnasioResponse>(`${this.API_URL_SUSCRIPCION}/${idGimnasio}`, null);
    }

    generarPago(idSuscripcion: number): Observable<string>{
        return this.http.post(`${this.API_URL_PAGOS}/${idSuscripcion}`, null, { responseType: 'text' })
    }
}
