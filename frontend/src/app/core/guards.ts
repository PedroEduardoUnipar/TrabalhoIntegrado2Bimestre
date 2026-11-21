import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => inject(AuthService).usuario() ? true : inject(Router).createUrlTree(['/login']);
export const adminGuard: CanActivateFn = () => inject(AuthService).admin() ? true : inject(Router).createUrlTree(['/catalogo']);
export const guestGuard: CanActivateFn = () => inject(AuthService).usuario() ? inject(Router).createUrlTree(['/catalogo']) : true;
