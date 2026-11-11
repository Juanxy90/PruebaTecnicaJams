# Ufinet Autos

Aplicación para que los usuarios registren y gestionen la información de sus autos.
Backend en **Java 21 + Spring Boot 3.5 (Maven)** con **arquitectura hexagonal** y **microservicios**,
frontend en **Angular 20**, base de datos **SQL Server** y todo **dockerizado**.

| Requisito | Dónde se cumple |
|---|---|
| Registro de usuario (BD) | `auth-service` → `POST /api/auth/register` (tabla `users`) |
| Login con JWT | `auth-service` → `POST /api/auth/login` (JJWT, HS256) |
| Acceso solo con token válido | Spring Security + `JwtAuthenticationFilter` en cada microservicio |
| CRUD de autos del usuario autenticado | `car-service` → `/api/cars` (el dueño siempre se toma del JWT) |
| Búsqueda por placa o modelo | `GET /api/cars?search=...` (patrón Specification) |
| Filtrado por año o marca | `GET /api/cars?brand=...&year=...` |
| Foto del auto (simulada) | Campo `photoUrl`; el frontend muestra vista previa local y guarda una URL ficticia |
| SQL Server + JPA/Hibernate | `database/01-schema.sql`, `02-seed.sql`; Hibernate en modo `validate` |
| Tabla `cars` con `user_id` FK | `fk_cars_users` → `users(id)` |
| Frontend: login, pantalla de autos, token | Angular (`/login`, `/register`, `/cars`), token en `localStorage` |
| Responsive | CSS grid/flex mobile-first, modales tipo *bottom sheet* en móvil |
| Pruebas unitarias | 83 backend (JUnit 5 + Mockito + MockMvc + DataJpaTest) y 17 frontend (Jasmine/Karma) |
| Colección Postman | `postman/Ufinet-Autos.postman_collection.json` |
| Lombok | `@Builder`, `@Getter`, `@RequiredArgsConstructor`, `@Slf4j`, etc. |

---

## 1. Arquitectura

```
                   ┌──────────────────────────┐
  Navegador ─────► │ frontend (Nginx :4200)   │  SPA Angular
                   │  /api/** ──► proxy       │
                   └────────────┬─────────────┘
                                │
                   ┌────────────▼─────────────┐
  Postman ───────► │ api-gateway (:8080)      │  Spring Cloud Gateway
                   │ rutas, CORS, X-Request-Id│
                   └──────┬────────────┬──────┘
          /api/auth/**    │            │   /api/cars/**
          /api/users/**   │            │
           ┌──────────────▼───┐   ┌────▼──────────────┐
           │ auth-service     │   │ car-service       │
           │ (:8081)          │   │ (:8082)           │
           │ registro, login, │   │ CRUD, búsqueda,   │
           │ emite JWT        │   │ filtros; valida   │
           │                  │   │ JWT               │
           └────────┬─────────┘   └─────────┬─────────┘
                    │      JPA/Hibernate    │
                    └──────────┬────────────┘
                     ┌─────────▼─────────┐
                     │ SQL Server (:1433)│  BD ufinet_autos
                     │ users ◄── cars    │  (cars.user_id FK)
                     └───────────────────┘
```

- **auth-service**: dueño de la tabla `users`. Registra usuarios (BCrypt), autentica y **emite** el JWT.
- **car-service**: dueño de la tabla `cars`. Solo **valida** el JWT (clave compartida) y usa el `sub`
  del token como id del dueño. Un usuario nunca puede ver/editar/eliminar autos de otro (responde 404).
- **api-gateway**: punto único de entrada; enruta por prefijo, centraliza CORS y agrega `X-Request-Id`.
- **frontend**: Nginx sirve la SPA y hace *reverse proxy* de `/api` al gateway (mismo origen, sin CORS).

> **Decisión de diseño — base de datos compartida.** El enunciado exige la tabla `cars` con `user_id`
> como llave foránea a `users`, por lo que ambos microservicios usan la misma base de datos, pero
> **cada uno solo mapea su propia tabla** (car-service no conoce la entidad `User`). Si en el futuro se
> quisiera *database-per-service*, bastaría con separar la BD y quitar la FK; ningún código cambia,
> porque car-service ya obtiene el usuario desde el JWT.

### 1.1 Arquitectura hexagonal (Ports & Adapters)

Cada microservicio de negocio tiene tres capas; las dependencias **siempre apuntan hacia el dominio**:

```
com.ufinet.autos.car
├── domain/                     ← Núcleo. Java puro (sin Spring, sin JPA)
│   ├── model/                  Car (agregado), CarDetails y CarSearchCriteria (Value Objects)
│   └── exception/              Excepciones de negocio (no conocen HTTP)
├── application/
│   ├── port/in/                Puertos de entrada: CreateCarUseCase, UpdateCarUseCase, ...
│   ├── port/out/               Puertos de salida: CarRepositoryPort
│   └── service/                Casos de uso (POJOs que implementan los puertos de entrada)
└── infrastructure/             ← Adaptadores (detalles técnicos)
    ├── adapter/in/web/         CarController, DTOs, mapper, GlobalExceptionHandler
    ├── adapter/out/persistence CarEntity (JPA), Spring Data repo, Specifications, adapter, mapper
    ├── security/               Filtro JWT, validador, SecurityConfig
    └── config/                 BeanConfiguration (composition root), Clock, OpenAPI
```

`auth-service` sigue exactamente la misma estructura (`User`, `RegisterUserUseCase`, `LoginUseCase`,
`UserRepositoryPort`, `PasswordHasherPort`, `TokenProviderPort`, ...).

Los casos de uso **no tienen anotaciones de Spring**: se instancian en `BeanConfiguration`.
Así el dominio y la aplicación se prueban con JUnit + Mockito sin levantar Spring.

---

## 2. Principios SOLID aplicados

| Principio | Cómo se aplica |
|---|---|
| **S** – Responsabilidad única | Un caso de uso por clase (`CreateCarService`, `UpdateCarService`, `DeleteCarService`, `CarQueryService`); mappers, controladores, adaptadores y manejador de errores separados. En Angular, `TokenStorageService` solo guarda el token y `AuthService` solo maneja la sesión. |
| **O** – Abierto/cerrado | Nuevos filtros de búsqueda = nueva `Specification` sin tocar las existentes. Cambiar BCrypt por Argon2 = nuevo adaptador de `PasswordHasherPort`. |
| **L** – Sustitución de Liskov | Cualquier implementación de `CarRepositoryPort` / `TokenProviderPort` (JPA, en memoria, mocks en tests) es intercambiable sin romper los casos de uso. |
| **I** – Segregación de interfaces | Puertos de entrada pequeños y específicos (`GetCarUseCase`, `SearchCarsUseCase`, `DeleteCarUseCase`...). El controlador depende solo de lo que usa. |
| **D** – Inversión de dependencias | La aplicación depende de abstracciones (`UserRepositoryPort`, `PasswordHasherPort`, `TokenProviderPort`); la infraestructura las implementa. Inyección por constructor (`@RequiredArgsConstructor`). |

---

## 3. Patrones de diseño utilizados

| Patrón | Dónde |
|---|---|
| **Hexagonal / Ports & Adapters** | Toda la estructura de `auth-service` y `car-service` |
| **Adapter** | `UserPersistenceAdapter`, `CarPersistenceAdapter`, `BCryptPasswordHasherAdapter`, `JwtTokenProvider` adaptan tecnologías a los puertos del dominio |
| **Repository** | `UserRepositoryPort`, `CarRepositoryPort` + Spring Data JPA |
| **Specification** | `CarSpecifications`: búsqueda por placa/modelo y filtros por marca/año combinables dinámicamente |
| **Factory Method** | `User.newUser(...)`, `Car.create(...)` crean entidades válidas aplicando las reglas del dominio |
| **Builder** | Lombok `@Builder` en entidades de dominio y JPA (`toBuilder` para copias inmutables) |
| **Value Object** | `CarDetails`, `CarSearchCriteria` (inmutables, auto-validados) |
| **DTO + Mapper (Data Mapper)** | `CarRequest/CarResponse` + `CarWebMapper`; `CarEntity` + `CarPersistenceMapper` |
| **Command** | `RegisterUserCommand`, `LoginCommand` (entrada de los casos de uso, independiente de HTTP) |
| **Strategy** | `PasswordHasherPort` / `TokenProviderPort`: algoritmo intercambiable |
| **Chain of Responsibility** | `JwtAuthenticationFilter` dentro del `SecurityFilterChain`; `GlobalFilter` del gateway |
| **API Gateway** | `api-gateway` con Spring Cloud Gateway |
| **Dependency Injection / Composition Root** | `BeanConfiguration` conecta puertos y adaptadores |
| **CQRS ligero** | Comandos (`Create/Update/DeleteCarService`) separados de consultas (`CarQueryService`) |
| **Singleton** | Beans de Spring y servicios Angular `providedIn: 'root'` |
| **Interceptor** (Angular) | `authInterceptor`: agrega el `Bearer` y cierra sesión ante un 401 |
| **Guard** (Angular) | `authGuard` / `guestGuard` protegen las rutas |
| **Observer** | RxJS (`debounceTime` + `switchMap` en la búsqueda) y Signals de Angular |
| **Container / Presentational** | `CarListComponent` (orquesta) vs `CarFormComponent` / `ConfirmDialogComponent` (presentacionales) |

---

## 4. API REST

Base: `http://localhost:8080` (gateway). Errores en formato **RFC 7807** (`application/problem+json`).

| Método | Endpoint | Auth | Descripción | Respuestas |
|---|---|---|---|---|
| POST | `/api/auth/register` | — | Registrar usuario (devuelve JWT) | 201, 400, 409 |
| POST | `/api/auth/login` | — | Login | 200, 400, 401 |
| GET | `/api/users/me` | JWT | Perfil del usuario autenticado | 200, 401 |
| GET | `/api/cars?search=&brand=&year=` | JWT | Listar / buscar / filtrar autos del usuario | 200, 400, 401 |
| GET | `/api/cars/brands` | JWT | Marcas del usuario (para el filtro) | 200, 401 |
| GET | `/api/cars/{id}` | JWT | Obtener un auto | 200, 401, 404 |
| POST | `/api/cars` | JWT | Crear auto (`Location` header) | 201, 400, 401, 409 |
| PUT | `/api/cars/{id}` | JWT | Editar auto | 200, 400, 401, 404, 409 |
| DELETE | `/api/cars/{id}` | JWT | Eliminar auto | 204, 401, 404 |

Ejemplo de cuerpo para crear/editar un auto:

```json
{ "brand": "Toyota", "model": "Corolla", "year": 2022, "plateNumber": "ABC-123", "color": "Rojo", "photoUrl": null }
```

Reglas: placa única (se normaliza a mayúsculas sin espacios, 5–10 caracteres `A-Z 0-9 -`),
año entre 1886 y el año actual + 1, textos obligatorios con longitud máxima.

**Swagger UI** (cada microservicio): http://localhost:8081/swagger-ui.html y http://localhost:8082/swagger-ui.html

---

## 5. Paso a paso con Docker (recomendado)

### Requisitos
- Docker Desktop (Windows/Mac) o Docker Engine + Compose v2 (Linux). Arquitectura **x86_64/amd64**
  (la imagen de SQL Server no tiene versión nativa ARM; en Mac M1/M2 active la emulación Rosetta en Docker Desktop).
- Puertos libres: `4200`, `8080`, `8081`, `8082`, `1433`.
- ~4 GB de RAM asignados a Docker (SQL Server necesita al menos 2 GB).

### Pasos

1. **(Opcional) Configurar variables.** Copie `.env.example` como `.env` y cambie la contraseña de
   SQL Server y el secreto JWT. Si no crea `.env`, se usan los valores por defecto.

   ```bash
   cp .env.example .env
   ```

2. **Construir y levantar todo** (desde la carpeta raíz del proyecto):

   ```bash
   docker compose up -d --build
   ```

   Qué sucede, en orden (los `depends_on` + `healthcheck` lo garantizan):
   1. `sqlserver` arranca y espera a estar *healthy*.
   2. `db-init` ejecuta `database/01-schema.sql` (BD + tablas + índices) y `02-seed.sql` (datos de prueba) y termina.
   3. `auth-service` y `car-service` se compilan **ejecutando las pruebas unitarias** dentro del build
      (multi-stage) y arrancan; Hibernate valida que el esquema coincida con las entidades.
   4. `api-gateway` arranca cuando ambos servicios están *healthy*.
   5. `frontend` (Angular compilado + Nginx) arranca al final.

   La primera vez tarda varios minutos (descarga de imágenes y dependencias Maven/npm).

3. **Verificar el estado:**

   ```bash
   docker compose ps
   ```

   Todos deben estar `Up (healthy)` excepto `ufinet-db-init`, que debe aparecer `Exited (0)`.

4. **Usar la aplicación:**
   - Frontend: http://localhost:4200
   - Usuario de prueba: `demo@ufinet.com` / `Password123` (también `maria@ufinet.com` / `Password123`)
   - API Gateway: http://localhost:8080/api
   - SQL Server (SSMS / Azure Data Studio): `localhost,1433`, usuario `sa`, contraseña de `.env`
     (por defecto `Ufinet#2026Strong`).

### Comandos útiles

```bash
docker compose logs -f auth-service car-service api-gateway
```

```bash
docker compose down
```

```bash
docker compose down -v
```

`down -v` borra también el volumen de SQL Server (la próxima vez se recrea la BD desde los scripts).

Reconstruir un solo servicio tras un cambio:

```bash
docker compose up -d --build car-service
```

Construir sin ejecutar las pruebas (más rápido):

```bash
docker compose build --build-arg SKIP_TESTS=true
```

---

## 6. Ejecución local sin Docker (desarrollo)

Requisitos: JDK 21+, Maven 3.9+, Node 20.19+/22/24, SQL Server.

1. Levante solo la BD con Docker (o use su propio SQL Server y ejecute los scripts de `database/`):
   ```bash
   docker compose up -d sqlserver db-init
   ```
2. Backend (una terminal por servicio, desde `backend/`):
   ```bash
   mvn -f auth-service/pom.xml spring-boot:run
   ```
   ```bash
   mvn -f car-service/pom.xml spring-boot:run
   ```
   ```bash
   mvn -f api-gateway/pom.xml spring-boot:run
   ```
   Variables opcionales: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`.
3. Frontend (usa `proxy.conf.json` para reenviar `/api` a `localhost:8080`):
   ```bash
   cd frontend && npm install && npm start
   ```
   Abra http://localhost:4200

---

## 7. Pruebas

Backend — todos los microservicios a la vez (desde `backend/`):

```bash
mvn clean verify
```

Sin Maven instalado, con Docker:

```bash
docker run --rm -v "$PWD/backend:/app" -w /app maven:3.9-eclipse-temurin-21 mvn -B verify
```

| Servicio | Pruebas | Qué cubren |
|---|---|---|
| auth-service | 32 | Dominio `User`, casos de uso (registro, login, perfil) con Mockito, `JwtTokenProvider` (firma, expiración, emisor), `AuthController` con seguridad real (`@WebMvcTest`), adaptador JPA (`@DataJpaTest` + H2) |
| car-service | 50 | Value Objects y reglas de `Car`, los 4 casos de uso, `JwtTokenValidator`, `CarController` con JWT real (401/400/404/409/201/204), Specifications de búsqueda y filtros (`@DataJpaTest`) |
| api-gateway | 1 | Registro de rutas |

Frontend (17 pruebas: `TokenStorageService`, `AuthService`, `authInterceptor`, `CarService`, validador de contraseñas, `App`):

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

---

## 8. Colección Postman

Importe `postman/Ufinet-Autos.postman_collection.json`. Ejecute **Autenticación → Login** primero:
el script guarda el JWT en la variable `token` y todas las demás peticiones lo usan automáticamente
(*Bearer Token* a nivel de colección). La colección incluye pruebas (`pm.test`) para cada respuesta,
por lo que puede ejecutarse completa con el **Collection Runner** o con Newman:

```bash
npx newman run postman/Ufinet-Autos.postman_collection.json
```

---

## 9. Estructura del repositorio

```
.
├── docker-compose.yml          Orquestación completa (BD, scripts, 3 microservicios, frontend)
├── .env.example                Variables configurables
├── database/
│   ├── 01-schema.sql           Creación de BD, tablas users/cars (FK), índices
│   └── 02-seed.sql             Usuarios y autos de prueba
├── backend/
│   ├── pom.xml                 POM agregador (mvn verify de todo)
│   ├── auth-service/           Microservicio de usuarios y JWT (hexagonal)
│   ├── car-service/            Microservicio de autos (hexagonal)
│   └── api-gateway/            Spring Cloud Gateway
├── frontend/                   Angular 20 (standalone components, signals)
│   ├── src/app/core/           Modelos, servicios, interceptor, guards
│   ├── src/app/features/       auth (login/registro) y cars (lista/formulario)
│   ├── src/app/shared/         Navbar, toasts, diálogo de confirmación
│   ├── nginx.conf              SPA + reverse proxy /api
│   └── Dockerfile
└── postman/                    Colección Postman
```

## 10. Seguridad

- Contraseñas con **BCrypt**; nunca se devuelven ni se registran en logs (`toString` las oculta).
- JWT **HS256** firmado con una clave de ≥256 bits configurable por variable de entorno; se validan
  firma, emisor (`iss`) y expiración (`exp`).
- API **stateless** (sin sesión ni CSRF); 401/403 en formato RFC 7807.
- El id del usuario se toma **siempre del token**, nunca del cuerpo de la petición.
- Mensaje de login genérico para no revelar qué correos existen.
- Contenedores Java ejecutados con usuario **no root**; solo el gateway y el frontend necesitan exponerse
  (8081/8082 se publican solo para Swagger).
- El token se guarda en `localStorage` (requisito del enunciado). En producción se recomienda
  complementarlo con una política CSP estricta o usar cookies `HttpOnly`.
