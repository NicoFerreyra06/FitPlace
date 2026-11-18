import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Ejercicio } from './ejercicio.model';
import { Observable } from 'rxjs';
import { Page } from '../../shared/models/page';

export interface EjercicioRequest {
  nombre: string;
  descripcion?: string;
  musculoPrincipalId: number[];
  musculoSecundarioId?: number[];
}

@Service()
export class EjercicioService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/ejercicios`;

  getAllEjercicio(): Observable<Page<Ejercicio>> {
    return this.http.get<Page<Ejercicio>>(this.apiUrl, { params: { size: 1000 } });
  }

  getEjercicioById(id: string | number): Observable<Ejercicio> {
    return this.http.get<Ejercicio>(`${this.apiUrl}/${id}`);
  }

  buscarPorNombre(nombre: string): Observable<Ejercicio[]> {
    return this.http.get<Ejercicio[]>(`${this.apiUrl}/buscar`, { params: { nombre } });
  }

  getEjerciciosPorMusculo(id: string | number): Observable<Ejercicio[]> {
    return this.http.get<Ejercicio[]>(`${this.apiUrl}/porMusculo/${id}`);
  }

  crearEjercicio(data: EjercicioRequest): Observable<Ejercicio> {
    return this.http.post<Ejercicio>(this.apiUrl, data);
  }

  actualizarEjercicio(id: number, data: EjercicioRequest): Observable<Ejercicio> {
    return this.http.put<Ejercicio>(`${this.apiUrl}/${id}`, data);
  }

  eliminarEjercicio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
