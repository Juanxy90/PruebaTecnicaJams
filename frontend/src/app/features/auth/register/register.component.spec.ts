import { FormControl, FormGroup } from '@angular/forms';
import { passwordsMatchValidator } from './register.component';

describe('passwordsMatchValidator', () => {
  const group = (password: string, confirmPassword: string) =>
    new FormGroup({ password: new FormControl(password), confirmPassword: new FormControl(confirmPassword) });

  it('es válido cuando las contraseñas coinciden', () => {
    expect(passwordsMatchValidator(group('Password123', 'Password123'))).toBeNull();
  });

  it('marca error cuando no coinciden', () => {
    expect(passwordsMatchValidator(group('Password123', 'Otra12345'))).toEqual({ passwordsMismatch: true });
  });
});
