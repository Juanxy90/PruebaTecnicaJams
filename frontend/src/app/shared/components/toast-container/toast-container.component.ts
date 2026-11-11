import { Component, inject } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of notifications.notifications(); track toast.id) {
        <div class="toast toast--{{ toast.type }}" role="status">
          <span>{{ toast.message }}</span>
          <button type="button" class="close" aria-label="Cerrar" (click)="notifications.dismiss(toast.id)">×</button>
        </div>
      }
    </div>
  `,
  styles: `
    .toasts {
      position: fixed; right: 16px; bottom: 16px; left: 16px; z-index: 100;
      display: flex; flex-direction: column; align-items: flex-end; gap: 8px; pointer-events: none;
    }
    .toast {
      pointer-events: auto; display: flex; align-items: center; gap: 12px;
      max-width: 420px; padding: 12px 14px; border-radius: var(--radius);
      color: #fff; box-shadow: var(--shadow-lg); animation: slide-in .2s ease-out;
    }
    .toast--success { background: var(--success); }
    .toast--error { background: var(--danger); }
    .toast--info { background: var(--primary); }
    .close { background: none; border: 0; color: inherit; font-size: 1.3rem; line-height: 1; cursor: pointer; }
    @keyframes slide-in { from { transform: translateY(10px); opacity: 0; } to { transform: none; opacity: 1; } }
  `,
})
export class ToastContainerComponent {
  protected readonly notifications = inject(NotificationService);
}
