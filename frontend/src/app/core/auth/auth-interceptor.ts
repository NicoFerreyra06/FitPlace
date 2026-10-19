import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from './auth';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const token = auth.getToken();
  const router = inject(Router);

  let request = req;
  if(token){
    request = req.clone({setHeaders: {Authorization: `Bearer ${token}`}});
  }

  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      if(err.status === 401 && token){
        auth.logOut();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};


