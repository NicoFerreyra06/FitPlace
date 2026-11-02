import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../../model/page';

export interface RecordPersonalResponse {
  id: number;
  ejercicioId: number;
  nombreUsuario: string;
  nombreEjercicio: string;
  pesoMaximo: number;
  fechaLogro: string;
}

/** Cantidad de filas que trae cada pedido del ranking. */
export const RANKING_TAMANIO_PAGINA = 10;

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

  /** Récord propio en un ejercicio. Responde 404 si todavía no hay récord. */
  getMiRecord(ejercicioId: number): Observable<RecordPersonalResponse> {
    return this.http.get<RecordPersonalResponse>(`${this.API_URL}/ejercicio/${ejercicioId}`);
  }

  /** Ranking de todos los usuarios en un ejercicio, del mayor peso al menor. */
  getRanking(
    ejercicioId: number,
    pagina = 0,
    tamanio = RANKING_TAMANIO_PAGINA,
  ): Observable<Page<RecordPersonalResponse>> {
    const params = new HttpParams().set('page', pagina).set('size', tamanio);
    return this.http.get<Page<RecordPersonalResponse>>(`${this.API_URL}/ranking/${ejercicioId}`, {
      params,
    });
  }
}
