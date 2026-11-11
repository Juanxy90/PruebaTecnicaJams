import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenStorageService } from './token-storage.service';
import { fakeJwt } from '../../testing/jwt';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('login guarda la sesión y actualiza el estado', () => {
    const token = fakeJwt(3600);
    let userName = '';

    service.login({ email: 'demo@ufinet.com', password: 'Password123' }).subscribe((u) => (userName = u.fullName));

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      accessToken: token,
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: { id: 1, fullName: 'Demo', email: 'demo@ufinet.com', createdAt: '' },
    });

    expect(userName).toBe('Demo');
    expect(service.isAuthenticated()).toBeTrue();
    expect(TestBed.inject(TokenStorageService).getToken()).toBe(token);
  });

  it('logout limpia la sesión y redirige a /login', () => {
    localStorage.setItem(TokenStorageService.TOKEN_KEY, fakeJwt(3600));

    service.logout('expired');

    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem(TokenStorageService.TOKEN_KEY)).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { reason: 'expired' } });
  });

  it('hasValidSession es falso sin token', () => {
    expect(service.hasValidSession()).toBeFalse();
  });
});
