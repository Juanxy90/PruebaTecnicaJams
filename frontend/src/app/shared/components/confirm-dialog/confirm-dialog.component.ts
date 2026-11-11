import { Component, HostListener, input, output } from '@angular/core';

/**
 * Diálogo de confirmación reutilizable (componente "presentacional":
 * solo recibe datos por input y notifica por output).
 */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="modal-backdrop" (click)="cancelled.emit()">
      <div class="modal modal--sm" role="alertdialog" aria-modal="true" [attr.aria-label]="title()"
           (click)="$event.stopPropagation()">
        <header class="modal-header">
          <h2>{{ title() }}</h2>
        </header>
        <div class="modal-body">
          <p>{{ message() }}</p>
        </div>
        <footer class="modal-footer">
          <button type="button" class="btn btn-ghost" (click)="cancelled.emit()" [disabled]="busy()">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmed.emit()" [disabled]="busy()">
            {{ busy() ? 'Procesando...' : confirmText() }}
          </button>
        </footer>
      </div>
    </div>
  `,
})
export class ConfirmDialogComponent {
  readonly title = input('Confirmar');
  readonly message = input.required<string>();
  readonly confirmText = input('Confirmar');
  readonly busy = input(false);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    if (!this.busy()) {
      this.cancelled.emit();
    }
  }
}
