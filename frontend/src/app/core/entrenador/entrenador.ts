import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface EntrenadorResponse {
    id: string,
    username: string,
}

@Service()
export class EntrenadorService {
    private readonly http = inject(HttpClient);
    private readonly API_URL = 'http://localhost:8080/usuarios';

    getEntrenadores(): Observable<EntrenadorResponse[]> {
        return this.http.get<EntrenadorResponse[]>(`${this.API_URL}/entrenadores`);
    }

    getMiEntrenador(): Observable<EntrenadorResponse> {
        return this.http.get<EntrenadorResponse>(`${this.API_URL}/me/entrenador`);
    }

    asignarEntrenador(entrenadorId: string): Observable<EntrenadorResponse> {
        return this.http.put<EntrenadorResponse>(`${this.API_URL}/me/entrenador/${entrenadorId}`, null);
    }

    desvincularEntrenador(): Observable<EntrenadorResponse> {
        return this.http.delete<EntrenadorResponse>(`${this.API_URL}/me/entrenador`);
    }

    
}
