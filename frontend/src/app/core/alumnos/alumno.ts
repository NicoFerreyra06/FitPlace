import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface AlumnoResponse {
    id: number;
    username: string;
    rol: string,
    peso: number | null;
    altura: number | null;
    imc: number | null;
    categoriaImc: string | null;
    rachaActualDias: number | null;
    rachaMaximaDias: number | null;
    rutinaActivaId: number | null;
}

@Service()
export class AlumnoService {
    private readonly http = inject(HttpClient);
    private readonly API_URL_ME = `${environment.apiUrl}/usuarios/me`;
    private readonly API_URL_USERS = `${environment.apiUrl}/usuarios`;
    
    getAlumnos(): Observable<AlumnoResponse[]> {
      return this.http.get<AlumnoResponse[]>(`${this.API_URL_ME}/alumnos`);
    }

    /** Responde 404 si el alumno no existe o no está a cargo del entrenador logueado. */
    getAlumnoById(id: string): Observable<AlumnoResponse> {
      return this.http.get<AlumnoResponse>(`${this.API_URL_ME}/alumnos/${id}`);
    }

    asignarRutina(idAlumno: number, idRutina: number): Observable<AlumnoResponse> {
      return this.http.put<AlumnoResponse>(`${this.API_URL_USERS}/${idAlumno}/rutina-activa/${idRutina}`, null);
}
  
    eliminarAlumno(idAlumno: number): Observable<AlumnoResponse> {
      return this.http.delete<AlumnoResponse>(`${this.API_URL_ME}/alumnos/${idAlumno}`);
    }
}
