import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../rutinas/rutinas.model';

export interface EjercicioResponse {
  id: number;
  nombre: string;
  descripcion: string;
}

@Service()
export class Ejercicios {
  private readonly http = inject(HttpClient);
  private readonly URL = 'http://localhost:8080/ejercicios';

  getTodos(): Observable<Page<EjercicioResponse>> {
    return this.http.get<Page<EjercicioResponse>>(this.URL, { params: { size: 200 } });
  }
}
