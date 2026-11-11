import { Injectable } from '@angular/core';
import { AuthResponse, User } from '../models/auth.models';

/**
 * Almacenamiento del JWT y del usuario en localStorage.
 * Única clase que conoce dónde se guarda el token (SRP): si mañana se cambia
 * a sessionStorage o cookies, solo se modifica este servicio.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  static readonly TOKEN_KEY = 'ufinet_autos.token';
  static readonly USER_KEY = 'ufinet_autos.user';

  save(auth: AuthResponse): void {
    localStorage.setItem(TokenStorageService.TOKEN_KEY, auth.accessToken);
    localStorage.setItem(TokenStorageService.USER_KEY, JSON.stringify(auth.user));
  }

  /** Devuelve el token solo si existe y no ha expirado. */
  getToken(): string | null {
    const token = localStorage.getItem(TokenStorageService.TOKEN_KEY);
    if (!token) {
      return null;
    }
    if (this.isExpired(token)) {
      this.clear();
      return null;
    }
    return token;
  }

  getUser(): User | null {
    const raw = localStorage.getItem(TokenStorageService.USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }

  clear(): void {
    localStorage.removeItem(TokenStorageService.TOKEN_KEY);
    localStorage.removeItem(TokenStorageService.USER_KEY);
  }

  /** Lee el claim "exp" del payload del JWT (no valida la firma: eso lo hace el backend). */
  isExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1];
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const { exp } = JSON.parse(json) as { exp?: number };
      return typeof exp !== 'number' || exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}
