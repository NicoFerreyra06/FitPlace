import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';
import { EjercicioList } from './features/components/ejercicios/ejercicio-list/ejercicio-list';
import { EjercicioDetails } from './features/components/ejercicios/ejercicio-details/ejercicio-details';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro},
  { path: 'home', component: Home, canActivate: [authGuard]},
  { path: 'ejercicios', component: EjercicioList, canActivate:[authGuard]},
  { path: '**', redirectTo: 'login'},
];
