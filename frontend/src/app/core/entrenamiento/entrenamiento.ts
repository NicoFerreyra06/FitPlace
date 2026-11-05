import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { Page } from '../../model/page';
import { Usuario } from '../usuario/usuario';

export interface MarcaEjercicio {
  id: number;
  ejercicioRutinaId: number;
  nombreEjercicio: string;
  pesoLevantado: number;
  repeticionesLogradas: number;
}

export interface EntrenamientoLog {
  id: number;
  idRutina: number;
  nombreUsuario: string;
  nombreRutina: string;
  fecha: string;
  marcasEjercicio: MarcaEjercicio[];
}

export interface MarcaEjercicioRequest {
  ejercicioRutinaId: number;
  pesoLevantado: number;
  repeticionesLogradas: number;
}

export interface EntrenamientoLogRequest {
  idRutina: number;
  marcasEjercicio: MarcaEjercicioRequest[];
}

/** Peso máximo levantado en un ejercicio en un día de entrenamiento. */
export interface PuntoProgresion {
  /** Formato yyyy-MM-dd. */
  fecha: string;
  pesoMaximo: number;
}

@Service()
export class Entrenamiento {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/entrenamientos';
  private readonly usuarioService = inject(Usuario);

  getHistorial(
    idUsuario: number,
    pagina = 0,
    desde?: string,
    hasta?: string,
  ): Observable<Page<EntrenamientoLog>> {
    let params = new HttpParams().set('page', pagina).set('sort', 'fecha,desc');
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    return this.http.get<Page<EntrenamientoLog>>(`${this.API_URL}/usuario/${idUsuario}`, {
      params,
    });
  }

  /**
   * Progresión de un usuario en un ejercicio, del entrenamiento más viejo al más nuevo.
   * El backend la manda como { [nombreEjercicio]: puntos }; acá se desenvuelve para que
   * los componentes reciban directamente la lista.
   */
  getProgresionEjercicio(idUsuario: number, idEjercicio: number): Observable<PuntoProgresion[]> {
    return this.http
      .get<Record<string, PuntoProgresion[]>>(
        `${this.API_URL}/usuario/${idUsuario}/ejercicio/${idEjercicio}`,
      )
      .pipe(map((porEjercicio) => Object.values(porEjercicio)[0] ?? []));
  }

  getById(id: number): Observable<EntrenamientoLog> {
    return this.http.get<EntrenamientoLog>(`${this.API_URL}/${id}`);
  }

  registrar(request: EntrenamientoLogRequest): Observable<EntrenamientoLog> {
    return this.http
      .post<EntrenamientoLog>(this.API_URL, request)
      .pipe(tap(() => this.usuarioService.limpiarPerfil()));
  }

  actualizar(id: number, request: EntrenamientoLogRequest): Observable<EntrenamientoLog> {
    return this.http
      .put<EntrenamientoLog>(`${this.API_URL}/${id}`, request)
      .pipe(tap(() => this.usuarioService.limpiarPerfil()));
  }

  eliminar(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(tap(() => this.usuarioService.limpiarPerfil()));
  }
}
