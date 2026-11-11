import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'info';

export interface AppNotification {
  id: number;
  type: NotificationType;
  message: string;
}

/**
 * Notificaciones tipo "toast" desacopladas de los componentes que las emiten.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private static readonly DURATION_MS = 3500;
  private nextId = 1;

  private readonly items = signal<AppNotification[]>([]);
  readonly notifications = this.items.asReadonly();

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message);
  }

  info(message: string): void {
    this.show('info', message);
  }

  dismiss(id: number): void {
    this.items.update((list) => list.filter((n) => n.id !== id));
  }

  private show(type: NotificationType, message: string): void {
    const id = this.nextId++;
    this.items.update((list) => [...list, { id, type, message }]);
    setTimeout(() => this.dismiss(id), NotificationService.DURATION_MS);
  }
}
