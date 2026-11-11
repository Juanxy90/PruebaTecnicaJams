# Guía completa del backend — Índice

Esta guía explica **todo** el backend: cada carpeta, cada paquete, cada clase y, en las clases
importantes, **cada línea de código**, junto con el *por qué* de cada decisión.

> Los bloques de código de esta guía son **copias comentadas** del código real. Los comentarios que
> empiezan con `// 👉` se agregaron solo para explicar; no existen en los archivos originales.

## Orden de lectura recomendado

| # | Archivo | Qué aprendes |
|---|---|---|
| 1 | [01-conceptos.md](01-conceptos.md) | Conceptos base: Maven, Spring Boot, inyección de dependencias, Lombok, records, arquitectura hexagonal, JWT, JPA, Spring Security. **Léelo primero** si algún término no te suena. |
| 2 | [02-auth-service.md](02-auth-service.md) | Microservicio de usuarios y login, clase por clase y línea por línea. |
| 3 | [03-car-service.md](03-car-service.md) | Microservicio de autos: CRUD, búsqueda, filtros y patrón Specification. |
| 4 | [04-api-gateway.md](04-api-gateway.md) | La puerta de entrada: rutas, CORS y filtro de logging. |
| 5 | [05-configuracion-docker-pruebas.md](05-configuracion-docker-pruebas.md) | `pom.xml`, `application.yml`, `Dockerfile`, el mapeo con las tablas SQL y **todas las pruebas**. |

## Mapa de carpetas del backend

```
backend/
├── pom.xml                                   POM "agregador": compila/prueba los 3 proyectos juntos
│
├── auth-service/                             MICROSERVICIO 1: usuarios + login + emisión de JWT
│   ├── Dockerfile                            Cómo se construye su imagen Docker
│   ├── pom.xml                               Dependencias y versión de Java/Spring
│   └── src/
│       ├── main/
│       │   ├── java/com/ufinet/autos/auth/   Código fuente (paquete base)
│       │   │   ├── AuthServiceApplication.java
│       │   │   ├── domain/                   CAPA 1: negocio puro
│       │   │   │   ├── model/                Entidades de negocio (User)
│       │   │   │   └── exception/            Errores de negocio
│       │   │   ├── application/              CAPA 2: casos de uso
│       │   │   │   ├── port/in/              Lo que el servicio OFRECE (interfaces)
│       │   │   │   ├── port/out/             Lo que el servicio NECESITA (interfaces)
│       │   │   │   └── service/              Implementación de los casos de uso
│       │   │   └── infrastructure/           CAPA 3: detalles técnicos
│       │   │       ├── adapter/in/web/       Entrada HTTP (controllers, DTOs)
│       │   │       ├── adapter/out/persistence/  Salida a SQL Server (JPA)
│       │   │       ├── adapter/out/security/ Salida a BCrypt y JWT
│       │   │       ├── security/             Configuración de Spring Security
│       │   │       └── config/               Configuración de Spring (beans)
│       │   └── resources/application.yml     Configuración (puerto, BD, JWT...)
│       └── test/java/com/ufinet/autos/auth/  Pruebas unitarias (misma estructura)
│
├── car-service/                              MICROSERVICIO 2: autos (misma estructura hexagonal)
│
└── api-gateway/                              MICROSERVICIO 3: enrutador (sin hexagonal: no tiene negocio)
```

## ¿Por qué `src/main/java/com/ufinet/autos/...`?

- `src/main/java` y `src/test/java` es la **convención de Maven**: Maven sabe que ahí está el código
  y las pruebas sin configurar nada.
- `com/ufinet/autos/auth` es el **paquete**. En Java, cada paquete es una carpeta. Se usa el dominio
  de la empresa al revés (`ufinet.com` → `com.ufinet`) para que los nombres sean únicos en el mundo.
- `src/main/resources` contiene archivos que no son código (configuración) y que se empaquetan dentro
  del `.jar`.
- `target/` (no versionado) es donde Maven deja lo compilado y el `.jar` final.

## Flujo completo de una petición (para tener el panorama)

Petición: el usuario logueado crea un auto → `POST http://localhost:8080/api/cars`

```
 1. api-gateway         RequestLoggingFilter agrega X-Request-Id
                        RoutesConfig: "/api/cars/**" → http://car-service:8082
 2. car-service         Tomcat recibe la petición
 3.   security          JwtAuthenticationFilter lee "Authorization: Bearer <jwt>"
                        JwtTokenValidator verifica firma + emisor + expiración
                        → guarda AuthenticatedUser(id=1,...) en el SecurityContext
                        (si el token es inválido → ProblemDetailSecurityHandler responde 401)
 4.   adapter/in/web    CarController.create(user, @Valid CarRequest)
                        (si el JSON no cumple @NotBlank, @Pattern... → GlobalExceptionHandler → 400)
                        CarWebMapper: CarRequest → CarDetails (el Value Object se auto-valida)
 5.   application       CreateCarUseCase.create(ownerId=1, details)  ← interfaz
                        CreateCarService: ¿placa existe? → Car.create(...) → repository.save(...)
 6.   domain            Car.create valida año y dueño, pone fechas
 7.   adapter/out       CarRepositoryPort.save(car)  ← interfaz
                        CarPersistenceAdapter: Car → CarEntity → saveAndFlush → INSERT en SQL Server
 8.   vuelta            CarEntity → Car → CarResponse → JSON con 201 Created + header Location
```
