import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { UsuarioPerfil } from '../usuario/usuario';

export interface AmigoResponse {
  id: number;
  username: string;
  rol: string;
  rachaActualDias: number | null;
}

@Service()
export class Amigos {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/usuarios/me/amigos';

  getAmigos(): Observable<AmigoResponse[]> {
    return this.http.get<AmigoResponse[]>(this.API_URL);
  }

  agregarAmigo(codigoAmigo: string): Observable<UsuarioPerfil> {
    return this.http.post<UsuarioPerfil>(
      `${this.API_URL}/${encodeURIComponent(codigoAmigo.trim())}`,
      null,
    );
  }

  eliminarAmigo(amigoId: number): Observable<UsuarioPerfil> {
    return this.http.delete<UsuarioPerfil>(`${this.API_URL}/${amigoId}`);
  }
}
