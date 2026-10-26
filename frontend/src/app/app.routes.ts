import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';
import { Perfil } from './features/perfil/perfil';
import { EjercicioList } from './features/components/ejercicios/ejercicio-list/ejercicio-list';
import { Layout } from './features/layout/layout';
import { Historial } from './features/entrenamientos/historial/historial';
import { MisRutinas } from './features/rutinas/mis-rutinas/mis-rutinas';
import { RutinaForm } from './features/rutinas/rutina-form/rutina-form';
import { RutinaDetails } from './features/rutinas/rutina-details/rutina-details';
import { EntrenamientoHoy } from './features/entrenamientos/entrenamiento-hoy/entrenamiento-hoy';
import { MisRecords } from './features/records/mis-records/mis-records';
import { RutinasPublicas } from './features/rutinas/rutinas-publicas/rutinas-publicas';
import { MisAmigos } from './features/amigos/mis-amigos/mis-amigos';

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
      { path: 'entrenamientos', component: Historial, canActivate: [authGuard] },
      { path: 'entrenamientos/hoy', component: EntrenamientoHoy, canActivate: [authGuard] },
      { path: 'rutinas', component: MisRutinas, canActivate: [authGuard] },
      { path: 'rutinas/publicas', component: RutinasPublicas, canActivate: [authGuard] },
      { path: 'rutinas/nueva', component: RutinaForm, canActivate: [authGuard] },
      { path: 'rutinas/:id', component: RutinaDetails, canActivate: [authGuard] },
      { path: 'rutinas/:id/editar', component: RutinaForm, canActivate: [authGuard] },
      { path: 'records', component: MisRecords, canActivate: [authGuard] },
      { path: 'amigos', component: MisAmigos, canActivate: [authGuard] },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
