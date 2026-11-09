import { Service, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
    costo: number,
    isMercadoPagoVinculado: boolean,
}

export type estadoSuscripcion = 'ACTIVA' | 'PENDIENTE' | 'VENCIDA' | 'CANCELADA'

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

    cancelarSuscripcion(idSuscripcion: number): Observable<SuscripcionGimnasioResponse>{
        return this.http.put<SuscripcionGimnasioResponse>(`${this.API_URL_SUSCRIPCION}/${idSuscripcion}/cancelar`, null);
    }

    generarPago(idSuscripcion: number): Observable<string>{
        return this.http.post(`${this.API_URL_PAGOS}/${idSuscripcion}`, null, { responseType: 'text' })
    }

    getSuscripcionesGimnasio(idGimnasio: number, estado: estadoSuscripcion | null): Observable<SuscripcionGimnasioResponse[]>{
        const params = estado ? new HttpParams().set('estado', estado) : new HttpParams();
        return this.http.get<SuscripcionGimnasioResponse[]>(`${this.API_URL_SUSCRIPCION}/gimnasio/${idGimnasio}`, { params });
    }

    activarSuscripcion(idSuscripcion: number, metodoPago: 'EFECTIVO' | 'TRANSFERENCIA'): Observable<SuscripcionGimnasioResponse>{
        return this.http.put<SuscripcionGimnasioResponse>(`${this.API_URL_SUSCRIPCION}/${idSuscripcion}/activar`, null, { params: { metodoPago } });
    }
}
