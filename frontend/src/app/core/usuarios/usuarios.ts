import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface UsuarioResponse {
  id: number;
  username: string;
  email: string;
  rol: string;
  rutinaActivaId: number | null;
}

@Service()
export class Usuarios {
  private readonly http = inject(HttpClient);
  private readonly URL = 'http://localhost:8080/usuarios';

  getMe(): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.URL}/me`);
  }

  activarRutina(idRutina: number): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${this.URL}/me/rutina-activa/${idRutina}`, null);
  }
}
