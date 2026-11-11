import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Solo deja pasar si hay un token vigente; si no, redirige a /login. */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  return authService.hasValidSession() ? true : inject(Router).createUrlTree(['/login']);
};

/** Evita que un usuario autenticado vuelva a /login o /register. */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  return authService.hasValidSession() ? inject(Router).createUrlTree(['/cars']) : true;
};
