import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';
import { MisRutinas } from './features/rutinas/mis-rutinas/mis-rutinas';
import { RutinaForm } from './features/rutinas/rutina-form/rutina-form';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro},
  { path: 'home', component: Home, canActivate: [authGuard]},
  { path: 'rutinas', component: MisRutinas, canActivate: [authGuard]},
  { path: 'rutinas/nueva', component: RutinaForm, canActivate: [authGuard]},
  { path: 'rutinas/:id/editar', component: RutinaForm, canActivate: [authGuard]},
];
