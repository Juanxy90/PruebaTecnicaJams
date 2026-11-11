import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guards';

/** Rutas con carga diferida (lazy loading) y protegidas por guards. */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cars' },
  {
    path: 'login',
    title: 'Iniciar sesión | Ufinet Autos',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    title: 'Crear cuenta | Ufinet Autos',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'cars',
    title: 'Mis autos | Ufinet Autos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/cars/car-list/car-list.component').then((m) => m.CarListComponent),
  },
  { path: '**', redirectTo: 'cars' },
];
