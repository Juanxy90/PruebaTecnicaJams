import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { TokenStorageService } from '../services/token-storage.service';
import { fakeJwt } from '../../testing/jwt';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let http: HttpTestingController;
  let authService: AuthService;
  const token = fakeJwt(3600);

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    localStorage.setItem(TokenStorageService.TOKEN_KEY, token);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('agrega el header Authorization a las peticiones de la API', () => {
    httpClient.get('/api/cars').subscribe();

    const req = http.expectOne('/api/cars');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush([]);
  });

  it('no agrega el token a los endpoints públicos de autenticación', () => {
    httpClient.post('/api/auth/login', {}).subscribe();

    const req = http.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('cierra la sesión cuando la API responde 401', () => {
    const logoutSpy = spyOn(authService, 'logout');

    httpClient.get('/api/cars').subscribe({ error: () => undefined });
    http.expectOne('/api/cars').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(logoutSpy).toHaveBeenCalledWith('expired');
  });
});
