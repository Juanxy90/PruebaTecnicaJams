import { Component, HostListener, inject, input, OnInit, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { Car, CarPayload } from '../../../core/models/car.models';
import { CarService } from '../../../core/services/car.service';
import { extractErrorMessage } from '../../../core/models/api-error';

type CarField = 'brand' | 'model' | 'year' | 'plateNumber' | 'color';

const MAX_PHOTO_BYTES = 5 * 1024 * 1024;

/**
 * Formulario (modal) para crear o editar un auto.
 * La foto es SIMULADA: se muestra una vista previa local y solo se envía al
 * backend una URL ficticia; el archivo nunca sale del navegador.
 */
@Component({
  selector: 'app-car-form',
  imports: [ReactiveFormsModule],
  templateUrl: './car-form.component.html',
  styleUrl: './car-form.component.scss',
})
export class CarFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly carService = inject(CarService);

  readonly car = input<Car | null>(null);
  readonly saved = output<Car>();
  readonly closed = output<void>();

  protected readonly maxYear = new Date().getFullYear() + 1;
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly photoPreview = signal<string | null>(null);
  protected readonly photoError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    brand: ['', [Validators.required, Validators.maxLength(50)]],
    model: ['', [Validators.required, Validators.maxLength(50)]],
    year: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1886),
      Validators.max(this.maxYear),
    ]),
    plateNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9-]{5,10}$/)]],
    color: ['', [Validators.required, Validators.maxLength(30)]],
    photoUrl: this.fb.control<string | null>(null),
  });

  get isEdit(): boolean {
    return this.car() !== null;
  }

  ngOnInit(): void {
    const car = this.car();
    if (car) {
      this.form.patchValue({
        brand: car.brand,
        model: car.model,
        year: car.year,
        plateNumber: car.plateNumber,
        color: car.color,
        photoUrl: car.photoUrl,
      });
    }
  }

  @HostListener('document:keydown.escape')
  protected close(): void {
    if (!this.submitting()) {
      this.closed.emit();
    }
  }

  protected normalizePlate(): void {
    const control = this.form.controls.plateNumber;
    control.setValue(control.value.replace(/\s+/g, '').toUpperCase());
  }

  protected onPhotoSelected(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files?.[0];
    this.photoError.set(null);
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.photoError.set('El archivo debe ser una imagen');
      fileInput.value = '';
      return;
    }
    if (file.size > MAX_PHOTO_BYTES) {
      this.photoError.set('La imagen no puede superar 5 MB');
      fileInput.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => this.photoPreview.set(reader.result as string);
    reader.readAsDataURL(file);

    // URL simulada: el archivo NO se sube al servidor.
    const safeName = file.name.toLowerCase().replace(/[^a-z0-9.]+/g, '-');
    this.form.controls.photoUrl.setValue(`/uploads/cars/${Date.now()}-${safeName}`);
  }

  protected removePhoto(): void {
    this.photoPreview.set(null);
    this.form.controls.photoUrl.setValue(null);
  }

  protected submit(): void {
    this.normalizePlate();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const payload: CarPayload = {
      brand: value.brand.trim(),
      model: value.model.trim(),
      year: value.year as number,
      plateNumber: value.plateNumber,
      color: value.color.trim(),
      photoUrl: value.photoUrl,
    };

    const car = this.car();
    const request$ = car ? this.carService.update(car.id, payload) : this.carService.create(payload);

    this.submitting.set(true);
    this.errorMessage.set(null);
    request$.pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: (savedCar) => this.saved.emit(savedCar),
      error: (error) => this.errorMessage.set(extractErrorMessage(error, 'No se pudo guardar el auto')),
    });
  }

  protected isInvalid(control: CarField): boolean {
    const c = this.form.controls[control];
    return c.invalid && (c.touched || c.dirty);
  }
}
