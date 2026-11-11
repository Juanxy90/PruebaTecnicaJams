import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  template: `
    <header class="navbar">
      <a class="brand" routerLink="/cars" aria-label="Ir a mis autos">
        <span class="brand-logo" aria-hidden="true">🚗</span>
        <span class="brand-name">Ufinet <strong>Autos</strong></span>
      </a>

      <div class="user">
        <span class="avatar" aria-hidden="true">{{ initials() }}</span>
        <span class="user-name">{{ auth.currentUser()?.fullName }}</span>
        <button type="button" class="btn btn-ghost btn-sm" (click)="auth.logout()">Cerrar sesión</button>
      </div>
    </header>
  `,
  styles: `
    .navbar {
      position: sticky; top: 0; z-index: 10;
      display: flex; align-items: center; justify-content: space-between; gap: 12px;
      padding: 12px 16px;
      background: var(--surface);
      border-bottom: 1px solid var(--border);
    }
    .brand { display: flex; align-items: center; gap: 8px; color: var(--text); text-decoration: none; font-size: 1.1rem; }
    .brand-logo { font-size: 1.5rem; }
    .brand-name strong { color: var(--primary); }
    .user { display: flex; align-items: center; gap: 10px; min-width: 0; }
    .avatar {
      display: grid; place-items: center; flex-shrink: 0;
      width: 34px; height: 34px; border-radius: 50%;
      background: var(--primary-soft); color: var(--primary); font-weight: 700; font-size: .85rem;
    }
    .user-name { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 220px; }
    @media (max-width: 600px) { .user-name, .brand-name { display: none; } }
  `,
})
export class NavbarComponent {
  protected readonly auth = inject(AuthService);

  protected readonly initials = computed(() => {
    const name = this.auth.currentUser()?.fullName ?? '';
    return name
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]!.toUpperCase())
      .join('');
  });
}
