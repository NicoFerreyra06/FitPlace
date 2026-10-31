import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { Usuario } from '../usuario/usuario';

export const rolGuard: CanActivateFn = (route) => {
  const usuarioService = inject(Usuario);
  const router = inject(Router);

  const rolesPermitidos: string[] | undefined = route.data['roles'];

  if (rolesPermitidos === undefined) return true;

  return usuarioService.getPerfilGuardado().pipe(
    map(user => rolesPermitidos.includes(user.rol)
        ? true
        : router.createUrlTree(['/home'])
    ),
    catchError((err: HttpErrorResponse) =>
      of(router.createUrlTree([err.status === 401 ? '/login' : '/home'])),
    )
  );
};