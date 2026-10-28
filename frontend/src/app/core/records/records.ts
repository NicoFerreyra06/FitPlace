import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface RecordPersonalResponse {
  id: number;
  ejercicioId: number;
  nombreUsuario: string;
  nombreEjercicio: string;
  pesoMaximo: number;
  fechaLogro: string;
}

@Service()
export class Records {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/records';

  getMisRecords(idUsuario: number): Observable<RecordPersonalResponse[]> {
    return this.http.get<RecordPersonalResponse[]>(`${this.API_URL}/usuario/${idUsuario}`);
  }

  getUltimoRecord(): Observable<RecordPersonalResponse> {
    return this.http.get<RecordPersonalResponse>(`${this.API_URL}/me/ultimo`);
  }
}
