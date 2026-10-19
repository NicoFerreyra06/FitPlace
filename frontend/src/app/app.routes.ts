import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Home } from './features/home/home';
import { authGuard } from './core/auth/auth-guard';
import { Registro } from './features/auth/registro/registro';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Registro },
  { path: 'home', component: Home, canActivate: [authGuard] },
  {path: 'perfil',
    loadComponent: () => import('./features/perfil/pages/perfil/perfil').then((m) => m.Perfil),
    canActivate: [authGuard]},
];
