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
import { RecordDetalle } from './features/records/record-detalle/record-detalle';
import { RutinasPublicas } from './features/rutinas/rutinas-publicas/rutinas-publicas';
import { MisAmigos } from './features/amigos/mis-amigos/mis-amigos';
import { AmigoDetalle } from './features/amigos/amigo-detalle/amigo-detalle';
import { GimnasioList } from './features/gimnasios/gimnasios-list/gimnasios';
import { GestionGimnasio } from './features/gimnasios/gestion-gimnasio/gestion-gimnasio';
import { Entrenador } from './features/entrenador/entrenador';
import { rolGuard } from './core/auth/rol-guard';
import { Alumnos } from './features/alumnos/mis-alumnos/alumnos';
import { AlumnoDetalle } from './features/alumnos/alumno-detalle/alumno-detalle/alumno-detalle';
import { NotFound } from './features/not-found/not-found';
import { UsuariosAdmin } from './features/administracion/usuarios/usuarios-admin';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro },
  {
    path: '',
    component: Layout,
    children: [
      { path: 'home', component: Home },
      { path: 'ejercicios', component: EjercicioList },
      { path: 'perfil', component: Perfil },
      { path: 'entrenamientos', component: Historial },
      { path: 'entrenamientos/hoy', component: EntrenamientoHoy },
      { path: 'rutinas', component: MisRutinas },
      { path: 'rutinas/publicas', component: RutinasPublicas },
      { path: 'rutinas/nueva', component: RutinaForm },
      { path: 'rutinas/:id', component: RutinaDetails },
      { path: 'rutinas/:id/editar', component: RutinaForm },
      { path: 'records', component: MisRecords },
      { path: 'records/:ejercicioId', component: RecordDetalle },
      { path: 'amigos', component: MisAmigos },
      { path: 'amigos/:id', component: AmigoDetalle },
      { path: 'gimnasios', component: GimnasioList },
      { path: 'gimnasios/:id/gestion', component: GestionGimnasio, canActivate: [rolGuard], data: {roles: ['ADMIN_GIMNASIO']} },
      { path: 'admin/usuarios', component: UsuariosAdmin, canActivate: [rolGuard], data: {roles: ['ADMIN']} },
      { path: 'entrenadores', component: Entrenador },
      { path: 'alumnos', component: Alumnos, canActivate: [rolGuard], data: {roles: ['ENTRENADOR']} },
      { path: 'alumnos/:id', component: AlumnoDetalle, canActivate: [rolGuard], data: {roles: ['ENTRENADOR']} }
    ],
    canActivate: [authGuard],
  },
  { path: '**', component: NotFound},
];
