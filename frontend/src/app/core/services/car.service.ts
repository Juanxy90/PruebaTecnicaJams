import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Car, CarFilters, CarPayload } from '../models/car.models';

/**
 * Cliente HTTP del microservicio de autos (vía API Gateway).
 * El token se agrega automáticamente en authInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class CarService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cars`;

  list(filters: CarFilters = {}): Observable<Car[]> {
    let params = new HttpParams();
    if (filters.search?.trim()) {
      params = params.set('search', filters.search.trim());
    }
    if (filters.brand?.trim()) {
      params = params.set('brand', filters.brand.trim());
    }
    if (filters.year != null) {
      params = params.set('year', String(filters.year));
    }
    return this.http.get<Car[]>(this.baseUrl, { params });
  }

  brands(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/brands`);
  }

  getById(id: number): Observable<Car> {
    return this.http.get<Car>(`${this.baseUrl}/${id}`);
  }

  create(payload: CarPayload): Observable<Car> {
    return this.http.post<Car>(this.baseUrl, payload);
  }

  update(id: number, payload: CarPayload): Observable<Car> {
    return this.http.put<Car>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
