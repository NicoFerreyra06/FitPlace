import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';
import { Page } from '../../model/page';

export type RolUsuario = 'USUARIO' | 'ENTRENADOR' | 'ADMIN' | 'ADMIN_GIMNASIO';
export type RolAsignable = Exclude<RolUsuario, 'ADMIN'>;

export interface UsuarioAdmin {
  id: number;
  username: string;
  email: string;
  rol: RolUsuario;
}

export interface GananciaGimnasio {
  nombreGimnasio: string;
  totalIngresosBrutos: number;
  comisionFitPlace: number;
  gananciaNetaGimnasio: number;
}

export interface UsuarioPerfil {
  id: number;
  username: string;
  email: string;
  rol: RolUsuario;
  codigoAmigo: string;
  peso: number;
  altura: number;
  imc: number;
  categoriaImc: string;
  rachaActualDias: number | null;
  rachaMaximaDias: number | null;
  rutinaActivaId: number | null;
}

/** Nombre legible de un rol del backend (USUARIO, ENTRENADOR, ADMIN, ADMIN_GIMNASIO). */
export function etiquetaRol(rol: string): string {
  switch (rol) {
    case 'ENTRENADOR':
      return 'Entrenador';
    case 'ADMIN':
      return 'Administrador';
    case 'ADMIN_GIMNASIO':
      return 'Admin de gimnasio';
    default:
      return 'Usuario';
  }
}

export interface PerfilUpdateRequest {
  altura: number;
  peso: number;
}

@Service()
export class Usuario {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/usuarios';

  private perfil: Observable<UsuarioPerfil> | null = null;

  getMiPerfil(): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>(`${this.API_URL}/me`);
  }

  getUsuarios(pagina: number): Observable<Page<UsuarioAdmin>> {
    const params = new HttpParams().set('page', pagina);
    return this.http.get<Page<UsuarioAdmin>>(this.API_URL, { params });
  }

  getUsuario(id: number): Observable<UsuarioAdmin> {
    return this.http.get<UsuarioAdmin>(`${this.API_URL}/${id}`);
  }

  cambiarRol(id: number, nuevoRol: RolAsignable): Observable<UsuarioAdmin> {
    const params = new HttpParams().set('nuevoRol', nuevoRol);
    return this.http.put<UsuarioAdmin>(`${this.API_URL}/${id}/rol`, null, { params });
  }

  getGananciasPorGimnasio(desde: string, hasta: string): Observable<GananciaGimnasio[]> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<GananciaGimnasio[]>(`${this.API_URL}/ganancias`, { params });
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
