import { inject, isDevMode } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (auth.estaLogueado() || (isDevMode() && state.url.includes('demo=1'))) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
