import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioPerfil } from '../models/usuario-perfil';

@Service()
export class PerfilApi {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/usuarios';

  getMiPerfil(): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>(`${this.API_URL}/me`);
  }
}
