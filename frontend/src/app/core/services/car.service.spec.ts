import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CarService } from './car.service';

describe('CarService', () => {
  let service: CarService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CarService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía solo los filtros con valor como query params', () => {
    service.list({ search: ' coro ', brand: '', year: 2022 }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/cars');
    expect(req.request.params.get('search')).toBe('coro');
    expect(req.request.params.has('brand')).toBeFalse();
    expect(req.request.params.get('year')).toBe('2022');
    req.flush([]);
  });

  it('usa los verbos REST correctos', () => {
    const payload = { brand: 'Toyota', model: 'Corolla', year: 2022, plateNumber: 'ABC-123', color: 'Rojo', photoUrl: null };

    service.create(payload).subscribe();
    service.update(1, payload).subscribe();
    service.delete(1).subscribe();

    expect(http.expectOne({ method: 'POST', url: '/api/cars' }).request.body).toEqual(payload);
    http.expectOne({ method: 'PUT', url: '/api/cars/1' });
    http.expectOne({ method: 'DELETE', url: '/api/cars/1' });
  });
});
