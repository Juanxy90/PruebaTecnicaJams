import { TestBed } from '@angular/core/testing';
import { TokenStorageService } from './token-storage.service';
import { AuthResponse } from '../models/auth.models';
import { fakeJwt } from '../../testing/jwt';

describe('TokenStorageService', () => {
  let service: TokenStorageService;

  const auth = (token: string): AuthResponse => ({
    accessToken: token,
    tokenType: 'Bearer',
    expiresIn: 3600,
    user: { id: 1, fullName: 'Demo', email: 'demo@ufinet.com', createdAt: '2026-01-01T10:00:00' },
  });

  beforeEach(() => {
    localStorage.clear();
    service = TestBed.inject(TokenStorageService);
  });

  afterEach(() => localStorage.clear());

  it('guarda y devuelve un token vigente y el usuario', () => {
    const token = fakeJwt(3600);
    service.save(auth(token));

    expect(service.getToken()).toBe(token);
    expect(service.getUser()?.email).toBe('demo@ufinet.com');
  });

  it('descarta un token expirado', () => {
    service.save(auth(fakeJwt(-10)));

    expect(service.getToken()).toBeNull();
    expect(localStorage.getItem(TokenStorageService.TOKEN_KEY)).toBeNull();
  });

  it('considera expirado un token malformado', () => {
    expect(service.isExpired('no-es-un-jwt')).toBeTrue();
  });

  it('clear elimina token y usuario', () => {
    service.save(auth(fakeJwt(3600)));
    service.clear();

    expect(service.getToken()).toBeNull();
    expect(service.getUser()).toBeNull();
  });
});
