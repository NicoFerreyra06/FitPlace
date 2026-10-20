import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Ejercicio } from '../../model/ejercicio';
import { Observable } from 'rxjs';
import { Page } from '../../model/page';

@Service()
export class EjercicioService {
    private http = inject(HttpClient);
    private apiUrl = 'http://localhost:8080/ejercicios';

    getAllEjercicio(): Observable<Page<Ejercicio>> {
        return this.http.get<Page<Ejercicio>>(this.apiUrl);
    }

    getEjercicioById(id: string | number): Observable<Ejercicio> {
        return this.http.get<Ejercicio>(`${this.apiUrl}/${id}`);
    }

    getEjercicioByNombre(nombre: string): Observable<Ejercicio> {
        return this.http.get<Ejercicio>(`${this.apiUrl}/${nombre}`);
    }
}


