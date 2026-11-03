import { Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Page } from '../../model/page';

export interface gimnasioResponse {
    id: number,
    nombre: string,
    direccion: string,
    precioCuota: number,
    idAdmin: number,
    nombreAdmin: string,
    diasDeApertura: string[],
    horarioApertura: string,
    horarioCierre: string,
    fotoUrl: string | null,
    isMercadoPagoVinculado: boolean,
}


@Service()
export class Gimnasios {
    private readonly http = inject(HttpClient);
    private readonly URL = 'http://localhost:8080/gimnasios';
    private readonly URL_MP = 'http://localhost:8080/mercadopago';

    private readonly DIAS: Record<string, string> = {
        MONDAY: 'Lunes',
        TUESDAY: 'Martes',
        WEDNESDAY: 'Miércoles',
        THURSDAY: 'Jueves',
        FRIDAY: 'Viernes',
        SATURDAY: 'Sábado',
        SUNDAY: 'Domingo',
    };

    getUrlVinculacion(idGimnasio: number): Observable<string> {
        return this.http.get(`${this.URL_MP}/vincular/${idGimnasio}`, { responseType: 'text' });
    }

    getMisGimnasios(): Observable<gimnasioResponse[]> {
        return this.http.get<gimnasioResponse[]>(`${this.URL}/me`).pipe(
            map(gimnasios => gimnasios.map(gimnasio => this.formatearGimnasio(gimnasio))),
        );
    }

    getAllGimnasios(pagina: number): Observable<Page<gimnasioResponse>>{
        const params = new HttpParams().set('page', pagina);
        return this.http.get<Page<gimnasioResponse>>(this.URL, { params }).pipe(
            map(page => ({
                ...page,
                content: page.content.map(gimnasio => this.formatearGimnasio(gimnasio)),
            })),
        );
    }

    private formatearGimnasio(gimnasio: gimnasioResponse): gimnasioResponse {
        return {
            ...gimnasio,
            diasDeApertura: gimnasio.diasDeApertura.map(dia => this.DIAS[dia]),
            horarioApertura: gimnasio.horarioApertura.slice(0, 5),
            horarioCierre: gimnasio.horarioCierre.slice(0, 5),
        };
    }
}
