import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface UsuarioPerfil {
  id: number;
  username: string;
  email: string;
  rol: string;
  codigoAmigo: string;
  peso: number;
  altura: number;
  imc: number;
  categoriaImc: string;
  rachaActualDias: number | null;
  rachaMaximaDias: number | null;
}

@Service()
export class Usuario {
  private readonly http = inject(HttpClient);

  getMiPerfil(): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>('http://localhost:8080/usuarios/me');
  }
}
