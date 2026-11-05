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

/** Perfil de un amigo (PerfilAmigoResponse): sin email ni código de amigo, a propósito. */
export interface PerfilAmigo {
  id: number;
  username: string;
  rol: string;
  peso: number | null;
  altura: number | null;
  rachaActualDias: number | null;
  rachaMaximaDias: number | null;
}

@Service()
export class Amigos {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/usuarios/me/amigos';

  getAmigos(): Observable<AmigoResponse[]> {
    return this.http.get<AmigoResponse[]>(this.API_URL);
  }

  /** Responde 404 si el usuario no existe y 403 si no está en el grupo de amigos. */
  getPerfilAmigo(amigoId: number): Observable<PerfilAmigo> {
    return this.http.get<PerfilAmigo>(`${this.API_URL}/${amigoId}/perfil`);
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
