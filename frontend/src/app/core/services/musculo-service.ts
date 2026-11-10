import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Musculo } from '../../model/musculo';
import { Page } from '../../model/page';

export interface MusculoRequest {
  nombre: string;
  grupoMuscular: string;
  funcionAnatomica?: string;
}

export interface GrupoMuscularInfo {
  valor: string;
  etiqueta: string;
}

export const GRUPOS_MUSCULARES: GrupoMuscularInfo[] = [
  { valor: 'PECHO', etiqueta: 'Pecho' },
  { valor: 'DORSAL', etiqueta: 'Espalda / Dorsal' },
  { valor: 'TRAPECIO', etiqueta: 'Trapecio' },
  { valor: 'HOMBRO_ANTERIOR', etiqueta: 'Hombro Anterior' },
  { valor: 'HOMBRO_LATERAL', etiqueta: 'Hombro Lateral' },
  { valor: 'HOMBRO_POSTERIOR', etiqueta: 'Hombro Posterior' },
  { valor: 'BICEPS', etiqueta: 'Bíceps' },
  { valor: 'TRICEPS', etiqueta: 'Tríceps' },
  { valor: 'ANTEBRAZOS', etiqueta: 'Antebrazos' },
  { valor: 'CORE', etiqueta: 'Core / Abdominales' },
  { valor: 'CUADRICEPS', etiqueta: 'Cuádriceps' },
  { valor: 'ISQUIOTIBIALES', etiqueta: 'Isquiotibiales' },
  { valor: 'GLUTEOS', etiqueta: 'Glúteos' },
  { valor: 'ADUCTORES', etiqueta: 'Aductores' },
  { valor: 'GEMELOS', etiqueta: 'Gemelos / Pantorrillas' },
];

export function etiquetaGrupoMuscular(grupo: string): string {
  const encontrado = GRUPOS_MUSCULARES.find((g) => g.valor === grupo);
  return encontrado ? encontrado.etiqueta : grupo;
}

@Service()
export class MusculoService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/musculos';

  getAllMusculos(): Observable<Page<Musculo>> {
    // El catálogo se usa completo en la gestión y en los selectores de ejercicios.
    return this.http.get<Page<Musculo>>(this.url, { params: { size: 1000 } });
  }

  crearMusculo(data: MusculoRequest): Observable<Musculo> {
    return this.http.post<Musculo>(this.url, data);
  }

  actualizarMusculo(id: string | number, data: MusculoRequest): Observable<Musculo> {
    return this.http.put<Musculo>(`${this.url}/${id}`, data);
  }

  eliminarMusculo(id: string | number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
