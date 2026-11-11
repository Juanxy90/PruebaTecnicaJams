# 01 — Conceptos base

Antes de leer las clases conviene entender las herramientas y las ideas que se usan en todas ellas.

---

## 1. Maven

Maven es la herramienta que **descarga librerías, compila, ejecuta las pruebas y empaqueta** el
proyecto. Todo se describe en el archivo `pom.xml` (*Project Object Model*).

Comandos que importan:

| Comando | Qué hace |
|---|---|
| `mvn compile` | Compila `src/main/java` → `target/classes` |
| `mvn test` | Compila y ejecuta las pruebas de `src/test/java` |
| `mvn package` | Lo anterior + genera `target/auth-service-1.0.0.jar` |
| `mvn verify` | Lo anterior + verificaciones finales (lo usamos para correr todo) |
| `mvn spring-boot:run` | Arranca la aplicación |

El `.jar` que genera Spring Boot es "ejecutable": trae adentro el servidor web (Tomcat) y todas las
librerías, así que se arranca con `java -jar app.jar`.

---

## 2. Spring Boot y la inyección de dependencias

### El contenedor de Spring
Al arrancar, Spring crea un **contenedor** (el *ApplicationContext*) que fabrica y guarda objetos
llamados **beans**. Luego "conecta" unos con otros: si la clase `A` necesita un `B` en su constructor,
Spring le pasa el bean `B`. Eso es **Inyección de Dependencias (DI)**.

```java
@RestController
@RequiredArgsConstructor               // Lombok genera el constructor con los campos final
public class AuthController {
    private final LoginUseCase loginUseCase;   // Spring busca un bean que implemente LoginUseCase
}                                              // y lo pasa al constructor automáticamente
```

**Por qué es bueno:** la clase no hace `new LoginService(...)`. No sabe qué implementación recibe,
solo la interfaz. Eso permite cambiar implementaciones y pasar *mocks* en las pruebas.

### Cómo se registran los beans
| Forma | Ejemplo | Cuándo se usa |
|---|---|---|
| Anotación de clase: `@Component`, `@Service`, `@Repository`, `@RestController`, `@Configuration` | `@Component class AuthWebMapper` | Clases de infraestructura. Spring las encuentra solo (*component scan*). |
| Método `@Bean` dentro de una clase `@Configuration` | `@Bean LoginUseCase loginUseCase(...) { return new LoginService(...); }` | Cuando la clase **no debe** tener anotaciones de Spring (nuestros casos de uso), o es de una librería externa (`BCryptPasswordEncoder`). |

### `@SpringBootApplication`
Es la suma de tres anotaciones:
- `@Configuration`: la clase puede declarar beans.
- `@EnableAutoConfiguration`: Spring Boot configura solo lo que encuentra en el classpath (si ve el
  driver de SQL Server y JPA, crea el DataSource y el EntityManager; si ve Spring Security, protege todo).
- `@ComponentScan`: busca clases anotadas **en el paquete de esa clase y sus subpaquetes**. Por eso
  `AuthServiceApplication` está en `com.ufinet.autos.auth`, la raíz.

---

## 3. Lombok

Lombok **genera código repetitivo al compilar**. No existe en tiempo de ejecución (por eso se excluye
del `.jar`). Las anotaciones que usamos:

| Anotación | Qué genera |
|---|---|
| `@Getter` | Un `getX()` por cada campo |
| `@Setter` | Un `setX(valor)` por cada campo (solo en entidades JPA, que lo necesitan) |
| `@RequiredArgsConstructor` | Un constructor con todos los campos `final` → base de la inyección por constructor |
| `@NoArgsConstructor` | Constructor vacío (JPA lo exige para crear entidades) |
| `@AllArgsConstructor(access = AccessLevel.PRIVATE)` | Constructor con todos los campos, **privado**: nadie de afuera puede hacer `new User(...)`; deben usar el builder o el factory method |
| `@Builder` | El patrón **Builder**: `User.builder().email("a").fullName("b").build()` |
| `@Builder(toBuilder = true)` | Además genera `user.toBuilder()`: copia el objeto para cambiarle algo (útil con objetos inmutables) |
| `@EqualsAndHashCode(of = "id")` | `equals`/`hashCode` comparando solo el `id` |
| `@ToString(exclude = "passwordHash")` | `toString()` sin el campo sensible |
| `@Slf4j` | Un campo `private static final Logger log = ...` para escribir logs |

Ejemplo de lo que genera `@RequiredArgsConstructor`:
```java
@RequiredArgsConstructor
public class LoginService {
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
}
// 👉 equivale a escribir a mano:
public LoginService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
}
```

---

## 4. Java moderno que aparece en el código

### `record`
Clase inmutable para "llevar datos". Java genera constructor, getters (sin el prefijo `get`),
`equals`, `hashCode` y `toString`.
```java
public record LoginCommand(String email, String rawPassword) { }
LoginCommand c = new LoginCommand("a@b.com", "123");
c.email();          // 👉 getter generado
```
**Constructor compacto**: permite validar o normalizar antes de asignar:
```java
public record CarSearchCriteria(Long ownerId, String text, String brand, Integer year) {
    public CarSearchCriteria {          // 👉 sin paréntesis: recibe los mismos parámetros
        text = blankToNull(text);       // 👉 se puede reasignar el parámetro antes de guardarlo
    }
}
```

### `Optional<T>`
Representa "puede haber un valor o no", en lugar de devolver `null`.
```java
repository.findByEmail(email)            // Optional<User>
    .filter(u -> hasher.matches(...))    // si no cumple, queda vacío
    .orElseThrow(InvalidCredentialsException::new);  // vacío → lanza excepción
```

### Lambdas y referencias a métodos
- `u -> u.getEmail()` es una función que recibe `u` y devuelve su email.
- `mapper::toDomain` es lo mismo que `entity -> mapper.toDomain(entity)`.
- `InvalidCredentialsException::new` es lo mismo que `() -> new InvalidCredentialsException()`.

### Streams
```java
cars.stream().map(mapper::toResponse).toList();   // 👉 transforma cada Car en CarResponse
```

### Text blocks (en las pruebas)
```java
String json = """
        {"email":"demo@ufinet.com"}
        """;
```

---

## 5. Arquitectura hexagonal (Ports & Adapters)

Idea central: **el negocio no debe depender de la tecnología**. La tecnología depende del negocio.

```
       ADAPTADORES DE ENTRADA                       ADAPTADORES DE SALIDA
       (quién nos llama)                            (a quién llamamos)
   ┌──────────────────┐                         ┌────────────────────────┐
   │ AuthController    │──► port/in ──► service ──► port/out ◄──│ UserPersistenceAdapter │──► SQL Server
   │ (HTTP/REST)       │    (interfaz)  (caso de    (interfaz)   │ JwtTokenProvider       │──► JJWT
   └──────────────────┘                 uso)                    │ BCryptPasswordHasher   │──► BCrypt
                                         │                      └────────────────────────┘
                                         ▼
                                      domain (User, reglas, excepciones)
```

| Término | Significado | En el código |
|---|---|---|
| **Dominio** | Las reglas del negocio, sin tecnología | `domain/model`, `domain/exception` |
| **Puerto de entrada** (*driving port*) | Interfaz con lo que la aplicación **ofrece** | `application/port/in/*UseCase` |
| **Puerto de salida** (*driven port*) | Interfaz con lo que la aplicación **necesita** | `application/port/out/*Port` |
| **Caso de uso** | Implementa un puerto de entrada usando puertos de salida | `application/service/*Service` |
| **Adaptador de entrada** | Traduce el mundo exterior → caso de uso | `adapter/in/web/*Controller` |
| **Adaptador de salida** | Implementa un puerto de salida con una tecnología | `adapter/out/persistence/*Adapter` |

**Regla de dependencias:** `infrastructure` → `application` → `domain`. Nunca al revés.
El dominio no importa nada de `org.springframework` ni de `jakarta.persistence`.

**Beneficios concretos:**
1. Probar la lógica sin base de datos ni servidor (pruebas rápidas con Mockito).
2. Cambiar SQL Server por otra BD = escribir otro adaptador; los casos de uso no cambian.
3. Cada clase tiene un propósito claro (fácil de encontrar y mantener).

---

## 6. JWT (JSON Web Token)

Un JWT son tres partes en Base64URL separadas por puntos: `header.payload.signature`.

```
eyJhbGciOiJIUzUxMiJ9 . eyJpc3MiOiJ1ZmluZXQtYXV0b3MtYXV0aCIsInN1YiI6IjEiLC4uLn0 . 3kD9...firma
   header                payload (claims)                                          firma
{"alg":"HS512"}        {"iss":"ufinet-autos-auth","sub":"1","email":"demo@ufinet.com",
                         "name":"Usuario Demo","iat":1794350000,"exp":1794353600}
```

| Claim | Significado |
|---|---|
| `iss` (issuer) | Quién emitió el token. car-service exige que sea `ufinet-autos-auth`. |
| `sub` (subject) | El id del usuario. |
| `email`, `name` | Datos extra (claims propios). |
| `iat` (issued at) | Cuándo se emitió. |
| `exp` (expiration) | Cuándo vence (60 minutos por defecto). |

- **La firma** se calcula con HMAC-SHA y una **clave secreta** que solo conocen los microservicios.
  Si alguien cambia un solo carácter del payload (por ejemplo, pone `sub: 2`), la firma ya no coincide
  y el token se rechaza.
- El payload **no está cifrado**, solo firmado: cualquiera puede leerlo, nadie puede modificarlo.
  Por eso nunca se pone la contraseña en el token.
- **Stateless**: el servidor no guarda sesiones. Cada petición trae el token y se valida solo con la
  firma, lo que permite escalar microservicios sin compartir sesiones.
- El algoritmo concreto (HS256/HS384/HS512) lo elige JJWT según el largo de la clave; nuestra clave
  de 64 bytes produce HS512.

---

## 7. BCrypt

Algoritmo para **guardar contraseñas**. No se puede "descifrar": para verificar se vuelve a calcular.

```
$2a$10$Ju9ZUrcIqgOcGDxP6iPrO.qcdjjoRPmqcEoITyPvD8GuF0hsjLonq
 │   │  └──── sal (22 chars) ─────┘└─────── hash (31 chars) ──┘
 │   └ costo 10 = 2^10 iteraciones (lento a propósito, contra fuerza bruta)
 └ versión del algoritmo
```
La **sal** es aleatoria: la misma contraseña genera hashes distintos cada vez. `matches(raw, hash)`
extrae la sal del hash guardado y repite el cálculo.

---

## 8. JPA / Hibernate / Spring Data

| Pieza | Qué es |
|---|---|
| **JPA** | Especificación estándar de Java para mapear clases ↔ tablas (`@Entity`, `@Column`...) |
| **Hibernate** | La implementación de JPA que traduce a SQL |
| **Spring Data JPA** | Genera los repositorios: declaras una interfaz y Spring escribe la implementación |
| **mssql-jdbc** | El driver que habla con SQL Server |

**Consultas derivadas del nombre del método**: Spring Data lee el nombre y genera el SQL.
```java
Optional<UserEntity> findByEmail(String email);
// 👉 SELECT * FROM users WHERE email = ?
boolean existsByPlateNumberAndIdNot(String plate, Long id);
// 👉 SELECT 1 FROM cars WHERE plate_number = ? AND id <> ? (limit 1)
```

**`ddl-auto: validate`**: Hibernate **no crea ni modifica tablas**. Al arrancar compara las entidades
con las tablas reales y, si algo no coincide (una columna falta o tiene otro tipo), el servicio **no
arranca**. Las tablas las crea el script `database/01-schema.sql`. Es lo recomendado en producción.

**Specification / Criteria API**: forma de construir el `WHERE` con código, combinando condiciones
solo cuando hacen falta (ver `CarSpecifications` en el archivo 03).

---

## 9. Spring Security en esta API

Spring Security es una **cadena de filtros** (*Chain of Responsibility*) que se ejecuta antes de
llegar al controller. Cada filtro hace su parte y pasa la petición al siguiente.

```
Petición ─► ... ─► JwtAuthenticationFilter ─► ... ─► AuthorizationFilter ─► Controller
                   (nuestro: lee el token y         (de Spring: ¿la ruta exige
                    pone el usuario en el            autenticación? ¿hay usuario?
                    SecurityContext)                 si no → 401)
```

- **SecurityContext**: "bolsillo" por petición donde se guarda quién es el usuario autenticado.
- **`@AuthenticationPrincipal`**: en el controller, saca ese usuario del bolsillo.
- **Stateless**: no hay sesión (cookie `JSESSIONID`), cada petición se autentica con su token.
- **CSRF desactivado**: CSRF es un ataque que aprovecha cookies de sesión; sin cookies no aplica.

---

## 10. ProblemDetail (RFC 7807)

Formato estándar para errores HTTP. Todos los errores de la API tienen esta forma:

```json
{
  "type": "about:blank",
  "title": "Conflicto",
  "status": 409,
  "detail": "Ya existe un auto registrado con la placa ABC-123",
  "instance": "/api/cars",
  "timestamp": "2026-11-10T21:39:31Z",
  "errors": { "plateNumber": "..." }        // 👉 solo en errores de validación
}
```
Spring 6 trae la clase `ProblemDetail`; basta con devolverla desde un `@ExceptionHandler`.

---

## 11. Códigos HTTP que usa la API

| Código | Cuándo |
|---|---|
| 200 OK | Lectura o edición correcta |
| 201 Created | Se creó un recurso (registro, auto). Incluye header `Location` con su URL |
| 204 No Content | Eliminación correcta (sin cuerpo) |
| 400 Bad Request | Datos inválidos o JSON mal formado |
| 401 Unauthorized | Sin token, token inválido/expirado, o credenciales incorrectas |
| 403 Forbidden | Autenticado pero sin permiso (no se da en esta app, pero está manejado) |
| 404 Not Found | No existe, **o pertenece a otro usuario** (para no revelar que existe) |
| 409 Conflict | Correo o placa duplicados |
| 500 Internal Server Error | Error inesperado (se registra en el log, al cliente se le da un mensaje genérico) |

---

## 12. Principios SOLID (resumen con ejemplos de este proyecto)

| Principio | Idea | Ejemplo |
|---|---|---|
| **S**ingle Responsibility | Una clase, un motivo para cambiar | `CreateCarService` solo crea; `CarWebMapper` solo convierte; `GlobalExceptionHandler` solo traduce errores |
| **O**pen/Closed | Abierto a extensión, cerrado a modificación | Nuevo filtro = nueva `Specification`, sin tocar las demás |
| **L**iskov Substitution | Cualquier implementación de una interfaz sirve | Las pruebas pasan *mocks* de `CarRepositoryPort` y los casos de uso funcionan igual |
| **I**nterface Segregation | Interfaces pequeñas | `GetCarUseCase`, `DeleteCarUseCase`... en vez de un `CarService` gigante |
| **D**ependency Inversion | Depender de abstracciones | `LoginService` depende de `PasswordHasherPort`, no de `BCryptPasswordEncoder` |
