import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { catchError, debounceTime, finalize, map, merge, of, startWith, Subject, switchMap, tap } from 'rxjs';
import { Car, CarFilters } from '../../../core/models/car.models';
import { CarService } from '../../../core/services/car.service';
import { NotificationService } from '../../../core/services/notification.service';
import { extractErrorMessage } from '../../../core/models/api-error';
import { CarFormComponent } from '../car-form/car-form.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

/**
 * Pantalla principal de autos: listar, buscar (placa/modelo), filtrar (marca/año),
 * agregar, editar y eliminar. Componente "contenedor" que orquesta servicios y
 * delega el formulario y la confirmación a componentes presentacionales.
 */
@Component({
  selector: 'app-car-list',
  imports: [ReactiveFormsModule, CarFormComponent, ConfirmDialogComponent],
  templateUrl: './car-list.component.html',
  styleUrl: './car-list.component.scss',
})
export class CarListComponent implements OnInit {
  private readonly carService = inject(CarService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  protected readonly cars = signal<Car[]>([]);
  protected readonly brands = signal<string[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editingCar = signal<Car | null>(null);
  protected readonly carToDelete = signal<Car | null>(null);
  protected readonly deleting = signal(false);

  protected readonly filters = this.fb.group({
    search: [''],
    brand: [''],
    year: this.fb.control<number | null>(null),
  });

  private readonly filterValues = toSignal(this.filters.valueChanges, { initialValue: this.filters.getRawValue() });

  protected readonly hasFilters = computed(() => {
    const f = this.filterValues();
    return !!f.search?.trim() || !!f.brand || f.year != null;
  });

  ngOnInit(): void {
    // Búsqueda reactiva: cada cambio de filtro (con debounce) o recarga dispara la consulta;
    // switchMap cancela la petición anterior si el usuario sigue escribiendo.
    merge(this.filters.valueChanges.pipe(debounceTime(300)), this.reload$)
      .pipe(
        startWith(null),
        map(() => this.currentFilters()),
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((filters) =>
          this.carService.list(filters).pipe(
            catchError((err) => {
              this.error.set(extractErrorMessage(err, 'No se pudieron cargar los autos'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((cars) => {
        this.loading.set(false);
        if (cars) {
          this.cars.set(cars);
        }
      });

    this.loadBrands();
  }

  protected reload(): void {
    this.reload$.next();
  }

  protected clearFilters(): void {
    this.filters.reset({ search: '', brand: '', year: null });
  }

  protected openCreate(): void {
    this.editingCar.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(car: Car): void {
    this.editingCar.set(car);
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    this.editingCar.set(null);
  }

  protected onSaved(car: Car): void {
    const wasEditing = this.editingCar() !== null;
    this.closeForm();
    this.notifications.success(
      wasEditing ? `Auto ${car.plateNumber} actualizado` : `Auto ${car.plateNumber} agregado`,
    );
    this.reload();
    this.loadBrands();
  }

  protected askDelete(car: Car): void {
    this.carToDelete.set(car);
  }

  protected confirmDelete(car: Car): void {
    this.deleting.set(true);
    this.carService
      .delete(car.id)
      .pipe(finalize(() => this.deleting.set(false)))
      .subscribe({
        next: () => {
          this.carToDelete.set(null);
          this.notifications.success(`Auto ${car.plateNumber} eliminado`);
          this.reload();
          this.loadBrands();
        },
        error: (err) => this.notifications.error(extractErrorMessage(err, 'No se pudo eliminar el auto')),
      });
  }

  private loadBrands(): void {
    this.carService
      .brands()
      .pipe(catchError(() => of([] as string[])), takeUntilDestroyed(this.destroyRef))
      .subscribe((brands) => this.brands.set(brands));
  }

  private currentFilters(): CarFilters {
    const { search, brand, year } = this.filters.getRawValue();
    const validYear = year != null && year >= 1886 && year <= 2100 ? year : null;
    return { search, brand, year: validYear };
  }
}
