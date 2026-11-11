export interface Car {
  id: number;
  brand: string;
  model: string;
  year: number;
  plateNumber: string;
  color: string;
  photoUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Cuerpo de las peticiones de creación/edición. */
export type CarPayload = Pick<Car, 'brand' | 'model' | 'year' | 'plateNumber' | 'color' | 'photoUrl'>;

/** Búsqueda por placa/modelo y filtros por marca/año. */
export interface CarFilters {
  search?: string | null;
  brand?: string | null;
  year?: number | null;
}
