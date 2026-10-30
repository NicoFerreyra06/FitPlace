import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';

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
  rutinaActivaId: number | null;
}

export interface PerfilUpdateRequest {
  altura: number;
  peso: number;
}

@Service()
export class Usuario {
  private readonly http = inject(HttpClient);

  private perfil: Observable<UsuarioPerfil> | null = null;

  getMiPerfil(): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>('http://localhost:8080/usuarios/me');
  }

  getPerfilGuardado (): Observable<UsuarioPerfil>{
    if(this.perfil === null) this.perfil = this.getMiPerfil().pipe(shareReplay(1));

    return this.perfil;
  }

  updateMiPerfil(perfilUpdateRequest: PerfilUpdateRequest): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>('http://localhost:8080/usuarios/me', perfilUpdateRequest).pipe(tap(()=> this.limpiarPerfil()));
  }

  activarRutina(idRutina: number): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>(`http://localhost:8080/usuarios/me/rutina-activa/${idRutina}`, null).pipe(tap(() => this.limpiarPerfil()));
  }

  desactivarRutina(): Observable<UsuarioPerfil> {
    return this.http.delete<UsuarioPerfil>('http://localhost:8080/usuarios/me/rutina-activa').pipe(tap(() => this.limpiarPerfil()));
  }

  limpiarPerfil(){
    this.perfil = null;
  }
}
