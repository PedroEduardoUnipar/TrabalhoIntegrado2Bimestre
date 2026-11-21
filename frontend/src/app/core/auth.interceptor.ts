import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);
  const api = request.url.startsWith('/api/');
  const publico = request.url === '/api/auth/login' || request.url === '/api/auth/cadastro';
  if (api && !publico && auth.token) request = request.clone({ setHeaders: { Authorization: `Bearer ${auth.token}` } });
  return next(request).pipe(catchError((error: HttpErrorResponse) => {
    if (api && !publico && error.status === 401) {
      auth.sair();
      toast.mostrar('Sua sessão expirou. Entre novamente.', 'erro');
    }
    return throwError(() => error);
  }));
};
