import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Ejercicio } from '../../model/ejercicio';
import { Observable } from 'rxjs';
import { Page } from '../../model/page';

export interface EjercicioRequest {
  nombre: string;
  descripcion?: string;
  musculoPrincipalId: number[];
  musculoSecundarioId?: number[];
}

@Service()
export class EjercicioService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/ejercicios';

  getAllEjercicio(): Observable<Page<Ejercicio>> {
    return this.http.get<Page<Ejercicio>>(this.apiUrl, { params: { size: 1000 } });
  }

  getEjercicioById(id: string | number): Observable<Ejercicio> {
    return this.http.get<Ejercicio>(`${this.apiUrl}/${id}`);
  }

  getEjercicioByNombre(nombre: string): Observable<Ejercicio> {
    return this.http.get<Ejercicio>(`${this.apiUrl}/${nombre}`);
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
