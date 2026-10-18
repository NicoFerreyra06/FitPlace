import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface authResponse{
    'token': string;
}

export interface authRequest{
    'email':string;
    'password': string;
}
@Service()
export class Auth {

    private readonly http = inject(HttpClient);
    private readonly TOKEN_KEY = 'token';

    login(authRequest: authRequest): Observable<authResponse>{
        return this.http.post<authResponse>('http://localhost:8080/usuarios/login', authRequest);
    }

    guardarToken (token: string){
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    estaLogueado():boolean{
        return this.getToken() ? true : false;
    }

    getToken(): string | null{
        return localStorage.getItem(this.TOKEN_KEY);
    }
}
