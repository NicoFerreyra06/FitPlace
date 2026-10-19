import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';
import { Perfil } from './features/perfil/perfil';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'perfil', component: Perfil, canActivate: [authGuard] },
];
