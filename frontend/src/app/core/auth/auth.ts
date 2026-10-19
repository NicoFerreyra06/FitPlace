import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export interface authResponse{
    'token': string;
}

export interface loginRequest{
    'email':string;
    'password': string;
}

export interface registerRequest{
    'username': string,
    'email': string,
    'password': string,
    'peso': number,
    'altura': number
}
@Service()
export class Auth {

    private readonly http = inject(HttpClient);
    private readonly TOKEN_KEY = 'token';

    login(authRequest: loginRequest): Observable<authResponse>{
        return this.http.post<authResponse>('http://localhost:8080/usuarios/login', authRequest);
    }

    register(registerRequest: registerRequest): Observable<authResponse>{
        return this.http.post<authResponse>('http://localhost:8080/usuarios/registro', registerRequest);
    }

    logOut(){
        localStorage.removeItem(this.TOKEN_KEY);
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
