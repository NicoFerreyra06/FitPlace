import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';
import { Perfil } from './features/perfil/perfil';
import { EjercicioList } from './features/components/ejercicios/ejercicio-list/ejercicio-list';
import { Layout } from './features/layout/layout';
import { MisRutinas } from './features/rutinas/mis-rutinas/mis-rutinas';
import { RutinaForm } from './features/rutinas/rutina-form/rutina-form';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro },
  {
    path: '',
    component: Layout,
    children: [
      { path: 'home', component: Home, canActivate: [authGuard] },
      { path: 'ejercicios', component: EjercicioList, canActivate: [authGuard] },
      { path: 'perfil', component: Perfil, canActivate: [authGuard] },
      { path: 'rutinas', component: MisRutinas, canActivate: [authGuard] },
      { path: 'rutinas/nueva', component: RutinaForm, canActivate: [authGuard] },
      { path: 'rutinas/:id/editar', component: RutinaForm, canActivate: [authGuard] },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
