import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { DiaRutinaResponse, Page, RutinaRequest, RutinaResponse } from './rutinas.model';

@Service()
export class Rutinas {
  private readonly http = inject(HttpClient);
  private readonly URL = 'http://localhost:8080/rutinas';

  getMisRutinas(): Observable<Page<RutinaResponse>> {
    return this.http.get<Page<RutinaResponse>>(`${this.URL}/me`, { params: { size: 50 } });
  }

  getRutina(id: number): Observable<RutinaResponse> {
    return this.http.get<RutinaResponse>(`${this.URL}/${id}`);
  }

  getDiaDeHoy(): Observable<DiaRutinaResponse> {
    return this.http.get<DiaRutinaResponse>(`${this.URL}/me/hoy`);
  }

  crear(rutina: RutinaRequest): Observable<RutinaResponse> {
    return this.http.post<RutinaResponse>(this.URL, rutina);
  }

  actualizar(id: number, rutina: RutinaRequest): Observable<RutinaResponse> {
    return this.http.put<RutinaResponse>(`${this.URL}/${id}`, rutina);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.URL}/${id}`);
  }
}
