import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface AlumnoResponse {
    id: number;
    username: string;
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
    private readonly API_URL = 'http://localhost:8080/usuarios/me';
    
    getAlumnos(): Observable<AlumnoResponse[]> {
      return this.http.get<AlumnoResponse[]>(`${this.API_URL}/alumnos`);
    }
  
    eliminarAlumno(idAlumno: number): Observable<AlumnoResponse> {
      return this.http.delete<AlumnoResponse>(`${this.API_URL}/alumnos/${idAlumno}`);
    }
}
