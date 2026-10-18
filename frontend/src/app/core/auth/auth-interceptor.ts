import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const token = auth.getToken();

  if(token){
    const nuevaRequest = req.clone({setHeaders: {Authorization: `Bearer ${token}`}});
    return next(nuevaRequest);
  } 

  return next(req);
};


