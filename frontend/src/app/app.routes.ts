import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { authGuard } from './core/auth/auth-guard';
import { rolGuard } from './core/auth/rol-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', loadComponent: () => import('./features/auth/registro/registro').then(m => m.Registro) },
  {
    path: '',
    loadComponent: () => import('./features/layout/layout').then(m => m.Layout),
    children: [
      { path: 'home', loadComponent: () => import('./features/home/home').then(m => m.Home) },
      { path: 'ejercicios', loadComponent: () => import('./features/components/ejercicios/ejercicio-list/ejercicio-list').then(m => m.EjercicioList) },
      { path: 'perfil', loadComponent: () => import('./features/perfil/perfil').then(m => m.Perfil) },
      { path: 'entrenamientos', loadComponent: () => import('./features/entrenamientos/historial/historial').then(m => m.Historial) },
      { path: 'entrenamientos/hoy', loadComponent: () => import('./features/entrenamientos/entrenamiento-hoy/entrenamiento-hoy').then(m => m.EntrenamientoHoy) },
      { path: 'rutinas', loadComponent: () => import('./features/rutinas/mis-rutinas/mis-rutinas').then(m => m.MisRutinas) },
      { path: 'rutinas/publicas', loadComponent: () => import('./features/rutinas/rutinas-publicas/rutinas-publicas').then(m => m.RutinasPublicas) },
      { path: 'rutinas/nueva', loadComponent: () => import('./features/rutinas/rutina-form/rutina-form').then(m => m.RutinaForm) },
      { path: 'rutinas/:id', loadComponent: () => import('./features/rutinas/rutina-details/rutina-details').then(m => m.RutinaDetails) },
      { path: 'rutinas/:id/editar', loadComponent: () => import('./features/rutinas/rutina-form/rutina-form').then(m => m.RutinaForm) },
      { path: 'records', loadComponent: () => import('./features/records/mis-records/mis-records').then(m => m.MisRecords) },
      { path: 'records/:ejercicioId', loadComponent: () => import('./features/records/record-detalle/record-detalle').then(m => m.RecordDetalle) },
      { path: 'amigos', loadComponent: () => import('./features/amigos/mis-amigos/mis-amigos').then(m => m.MisAmigos) },
      { path: 'amigos/:id', loadComponent: () => import('./features/amigos/amigo-detalle/amigo-detalle').then(m => m.AmigoDetalle) },
      { path: 'gimnasios', loadComponent: () => import('./features/gimnasios/gimnasios-list/gimnasios').then(m => m.GimnasioList) },
      { path: 'gimnasios/:id/gestion', loadComponent: () => import('./features/gimnasios/gestion-gimnasio/gestion-gimnasio').then(m => m.GestionGimnasio), canActivate: [rolGuard], data: {roles: ['ADMIN_GIMNASIO']} },
      { path: 'entrenadores', loadComponent: () => import('./features/entrenador/entrenador').then(m => m.Entrenador) },
      { path: 'alumnos', loadComponent: () => import('./features/alumnos/mis-alumnos/alumnos').then(m => m.Alumnos), canActivate: [rolGuard], data: {roles: ['ENTRENADOR']} },
      { path: 'alumnos/:id', loadComponent: () => import('./features/alumnos/alumno-detalle/alumno-detalle/alumno-detalle').then(m => m.AlumnoDetalle), canActivate: [rolGuard], data: {roles: ['ENTRENADOR']} }
    ],
    canActivate: [authGuard],
  },
  { path: '**', loadComponent: () => import('./features/not-found/not-found').then(m => m.NotFound) },
];
