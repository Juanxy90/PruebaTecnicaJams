import { HttpErrorResponse } from '@angular/common/http';

/** Formato de error RFC 7807 que devuelven los microservicios. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

/** Obtiene un mensaje legible para el usuario a partir de un error HTTP. */
export function extractErrorMessage(error: unknown, fallback = 'Ocurrió un error inesperado'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.';
    }
    const body = error.error as ProblemDetail | null;
    if (body?.errors && Object.keys(body.errors).length > 0) {
      return Object.values(body.errors).join('. ');
    }
    if (body?.detail) {
      return body.detail;
    }
  }
  return fallback;
}
