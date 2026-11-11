/**
 * La API se consume siempre por ruta relativa:
 *  - En desarrollo (ng serve) el proxy (proxy.conf.json) la reenvía al API Gateway (localhost:8080).
 *  - En Docker, Nginx la reenvía al contenedor api-gateway.
 */
export const environment = {
  apiUrl: '/api',
};
