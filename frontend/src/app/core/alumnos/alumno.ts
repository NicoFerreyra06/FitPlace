import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { map, Observable } from 'rxjs';

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
    private readonly API_URL_ME = 'http://localhost:8080/usuarios/me';
    private readonly API_URL_USERS = 'http://localhost:8080/usuarios';
    
    getAlumnos(): Observable<AlumnoResponse[]> {
      return this.http.get<AlumnoResponse[]>(`${this.API_URL_ME}/alumnos`);
    }

    getAlumnoById(id: string): Observable<AlumnoResponse | null> {
      return this.getAlumnos().pipe(map((alumnos) => alumnos.find((alumno) => String(alumno.id) === id) ?? null));
    }

    asignarRutina(idAlumno: number, idRutina: number): Observable<AlumnoResponse> {
      return this.http.put<AlumnoResponse>(`${this.API_URL_USERS}/${idAlumno}/rutina-activa/${idRutina}`, null);
}
  
    eliminarAlumno(idAlumno: number): Observable<AlumnoResponse> {
      return this.http.delete<AlumnoResponse>(`${this.API_URL_ME}/alumnos/${idAlumno}`);
    }
}
