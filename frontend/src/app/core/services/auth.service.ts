import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { TokenStorageService } from './token-storage.service';

/**
 * Gestiona la sesión del usuario (login, registro, logout) y expone el estado
 * como signals para que la UI reaccione automáticamente (patrón Observer).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(TokenStorageService);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly user = signal<User | null>(this.storage.getToken() ? this.storage.getUser() : null);

  readonly currentUser = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.user() !== null);

  login(request: LoginRequest): Observable<User> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => this.startSession(response)),
      map((response) => response.user),
    );
  }

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap((response) => this.startSession(response)),
      map((response) => response.user),
    );
  }

  logout(reason?: 'expired'): void {
    this.storage.clear();
    this.user.set(null);
    void this.router.navigate(['/login'], { queryParams: reason ? { reason } : {} });
  }

  /** true si hay un token vigente; sincroniza el estado si el token expiró. */
  hasValidSession(): boolean {
    const valid = this.storage.getToken() !== null;
    if (!valid && this.user() !== null) {
      this.user.set(null);
    }
    return valid;
  }

  private startSession(response: AuthResponse): void {
    this.storage.save(response);
    this.user.set(response.user);
  }
}
