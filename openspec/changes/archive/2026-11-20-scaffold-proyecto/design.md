# Design

## Context

El repositorio tiene la fundación (KB, roadmap, reglas) y ningún código. La motivación está en `proposal.md`. Restricciones que condicionan el diseño:

- **Stack fijado en la KB**: Java 17, Spring Boot (4.2, ver D1), Maven Wrapper, JPA + H2, Thymeleaf y Spring Security (`knowledge-base/02` y `08`, DD-02 a DD-05).
- **Dueño de lo compartido**: este change es dueño de **todos** los archivos compartidos. Después del merge, nadie de la ola 1 puede editarlos (reglas E1 y J5 de `AGENTS.md`, reglas anti-colisión de `CHANGES.md`). Por eso tiene que dejar resuelto, desde el arranque, todo lo que C-02..C-07 van a necesitar.
- **Entorno**: la máquina del líder tiene JDK 17 (Oracle 17.0.12), sin Maven ni Gradle, y no tiene `JAVA_HOME` definido.
- **Plazo**: 2026-11-25. Este change bloquea a otros cinco y tiene que mergearse el 2026-11-21.

## Goals / Non-Goals

**Goals:**
- Que cada change de la ola 1 se implemente agregando archivos **solo en su subpaquete**, sin tocar nada de este change.
- Que las reglas de C-02 se puedan testear con un reloj fijo y datos ficticios sin escribir infraestructura nueva.
- Que `./mvnw test` funcione desde un clon limpio.

**Non-Goals:**
- Ningún endpoint de negocio, caso de uso ni regla RN-TU/RN-CA (son de C-02..C-08).
- Migraciones versionadas (Flyway o Liquibase). H2 se regenera en cada arranque; las migraciones llegan con PostgreSQL.
- Seguridad real (C-05) y vistas Thymeleaf (C-09/C-10).

## Decisions

### D1. Generación del proyecto con Spring Initializr, Spring Boot 4.2.0
Se genera el esqueleto con la API oficial de `start.spring.io`: Maven, Java 17, **Spring Boot 4.2.0** y los starters listados en `proposal.md`. Así se obtiene un `pom.xml` y un Maven Wrapper consistentes sin tener Maven instalado.
- **Cambio durante el apply (2026-11-20, aprobado por el líder)**: la KB fijaba "Spring Boot 3", pero Initializr ya solo ofrece 4.x y la última 3.x (3.5.16) no recibe más parches. Se eligió 4.2.0. Consecuencias para el equipo: starters con nombres de Boot 4 (`spring-boot-starter-webmvc`, starters de test por módulo como `spring-boot-starter-webmvc-test`), Jackson 3 (`tools.jackson.databind`), Spring Security 7, H2 console como módulo `spring-boot-h2console`, y algunas auto-configuraciones y anotaciones de test movidas a paquetes por módulo.
- *Alternativa*: Spring Boot 3.5.16, generada con 4.x y bajada a mano. Se descartó por estar fuera de soporte.
- *Alternativa*: escribir el `pom.xml` y el wrapper a mano. Se descartó porque es propenso a errores de versión y el wrapper necesita los archivos `.mvn/wrapper`.
- Paquete base `ar.odonto.turnos`, artefacto `turnos-odontologia`.

### D2. Entidades JPA en `domain/model` (cierra DD-03)
Las entidades llevan anotaciones `jakarta.persistence` directamente en `domain/model`. **No** hay un modelo de dominio separado con mapeo.
- *Alternativa*: modelo puro más entidades JPA espejo en `infrastructure/persistence`. Se descartó porque duplica 8 clases y sus mapeos, y con 5 días y 6 integrantes es el doble de superficie de conflicto.
- *Cómo se respeta J1*: la regla prohíbe anotaciones Spring/JPA en `domain/rules` (las reglas), no en el modelo. Las reglas de C-02 en adelante reciben objetos del modelo o value objects y no dependen de Spring.
- Cada entidad valida sus invariantes en el constructor o en un *factory* (por ejemplo `HorarioAtencion` con `apertura < cierre` → `HORARIO_INVALIDO`, y `Prestacion` con duración mayor que 0 y múltiplo de 5 → `DURACION_INVALIDA`). Tienen un constructor `protected` sin argumentos para JPA y no exponen *setters* públicos para los campos con invariantes.
- `Turno` referencia `Profesional`, `Sillon`, `Paciente` y `Prestacion` con `@ManyToOne(fetch = LAZY)`. Persiste `inicio` y `fin` como `LocalDateTime` e incluye `sobreturno`, `estado` (`RESERVADO`/`CANCELADO`), `canceladoEn`, `creadoEn` y `creadoPor`. Tiene índices `(profesional_id, inicio)`, `(sillon_id, inicio)` y `(paciente_id, inicio)`.
- `spring.jpa.open-in-view=false`, para que ningún controlador dependa de la carga perezosa.

### D3. Puertos en `domain/port`, implementados por Spring Data
Cada repositorio es una interfaz Java pura en `domain/port` (`TurnoRepository`, `ProfesionalRepository`, …). En `infrastructure/persistence`, una interfaz Spring Data la implementa: `interface JpaTurnoRepository extends TurnoRepository, JpaRepository<Turno, Long>`, con `@Query` en las consultas no derivables.
- *Alternativa*: un adaptador a mano por repositorio. Se descartó porque es más código sin beneficio para el MVP.
- Los casos de uso de la ola 1 dependen **solo** de los puertos. Si un change necesita una consulta nueva, la declara en una interfaz propia de su paquete (regla anti-colisión 4).

### D4. Consultas de choque centralizadas
`TurnoRepository` expone las consultas que necesita C-02 (y que C-08 reutiliza para probar que se libera el horario), siempre sobre turnos `RESERVADO`:
- `buscarChoquesDeProfesional(profesionalId, desde, hasta, excluirTurnoId)`
- `buscarChoquesDeSillon(sillonId, desde, hasta, excluirTurnoId)`
- `buscarSuperpuestosDePaciente(pacienteId, inicio, fin, excluirTurnoId)`
- `buscarActivosDeProfesionalEntre(profesionalId, desdeDia, hastaDia)` (agenda, C-07)

Con margen `M`, el choque de `N` con un `E` existente es `N.inicio < E.fin + M` **y** `E.inicio < N.fin + M`. Para que la consulta use los índices, se reescribe como `E.fin > N.inicio - M` **y** `E.inicio < N.fin + M`: quien llama pasa `desde = N.inicio - M` y `hasta = N.fin + M`. El paciente usa `M = 0`. `excluirTurnoId` acepta `null` y deja lista la reprogramación futura.
- **Tests de este change**: verifican **solo la consulta** (bordes 10:04 y 10:05, `CANCELADO` excluido, otro recurso no choca). La regla de negocio que las usa y su orden de validación son de C-02.

### D5. Tiempo: un único `Clock` en la zona del consultorio
Bean `Clock` = `Clock.system(ZoneId.of(turnos.zona-horaria))`. Las fechas de negocio son `LocalDateTime` en esa zona y se guardan sin conversión. En los tests se reemplaza por `Clock.fixed(...)` con el helper `RelojDePrueba` de `support/`.
- *Alternativa*: `OffsetDateTime` o `Instant` en la base. Se descartó porque el negocio piensa en hora local de Argentina, no hay multi-zona y simplifica la API (ISO-8601 sin offset, RN-GL-01).

### D6. Configuración tipada `turnos.*`
Un record `@ConfigurationProperties("turnos") TurnosProperties(int margenMinutos = 5, int cancelacionHorasMinimas = 6, String zonaHoraria = "America/Argentina/Buenos_Aires")`, validado con `@Validated`. Se puede sobrescribir con las variables `TURNOS_MARGEN_MINUTOS`, `TURNOS_CANCELACION_HORAS_MINIMAS` y `TURNOS_ZONA_HORARIA` (relaxed binding de Spring).

### D7. Errores: catálogo, excepción y manejador global
- `CodigoError` (enum en `domain/error`): cada valor tiene su `HttpStatus` y un título en español. Trae todos los códigos de la spec.
- `ReglaDeNegocioException(CodigoError codigo, String detalle)`: es la única excepción que lanzan las reglas.
- `ManejadorGlobalDeErrores` (`web/api/error`, `@RestControllerAdvice` que extiende `ResponseEntityExceptionHandler`) construye un `ProblemDetail` con la propiedad `codigo`, según este mapeo:

| Excepción | Respuesta |
|---|---|
| `ReglaDeNegocioException` | su código |
| `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException` | `ENTRADA_INVALIDA`, con el campo que falló |
| `DataIntegrityViolationException` por restricción única | `DATO_DUPLICADO` |
| Cualquier otra | `ERROR_INTERNO` (500) |

  En el caso de `ERROR_INTERNO`, se loguea en el servidor sin datos de pacientes (D2) y la respuesta nunca incluye la traza.

### D8. Seguridad permisiva como auto-configuración reemplazable
Hay un problema con el reemplazo: `@ConditionalOnMissingBean` en una `@Configuration` normal depende del orden de carga y no es confiable. Además, Spring Security 6 falla si conviven dos cadenas que matchean "cualquier request".
- **Solución**: la configuración permisiva es una **auto-configuración** propia (`PermisivaSecurityAutoConfiguration`), registrada en `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` con `@AutoConfiguration(before = SecurityAutoConfiguration.class)` y `@ConditionalOnMissingBean(SecurityFilterChain.class)`. Las auto-configuraciones se evalúan **después** de la configuración del usuario, así que, cuando C-05 declara su `SecurityFilterChain` en `infrastructure/security`, la permisiva se desactiva sola sin editar nada de C-01.
- La cadena permisiva hace `permitAll` y deshabilita CSRF (solo mientras no exista C-05).
- En la misma auto-configuración va el bean por defecto de `UsuarioActual` (puerto en `domain/port`), que devuelve un `Solicitante` (record en `domain/model`: `rol`, `usuarioId`, `profesionalId` opcional) con rol `ADMIN` y usuario `sistema`. También es `@ConditionalOnMissingBean`, y C-05 lo reemplaza.
- El `PasswordEncoder` (BCrypt) es compartido y vive en `infrastructure/config`, porque lo usan el seed y C-05.

### D9. Seed de desarrollo idempotente
`DatosDeEjemploDev` (`infrastructure/config`, `@Profile("dev")`, `ApplicationRunner`) carga los datos solo si la base está vacía. Los usuarios se crean solo si están definidas `SEED_ADMIN_PASSWORD`, `SEED_SECRETARIA_PASSWORD` y `SEED_ODONTOLOGO_PASSWORD`. Si falta alguna, loguea "usuarios de ejemplo deshabilitados: definir SEED_*_PASSWORD" sin valores. En `application-dev.yml` se habilita la consola H2 en `/h2-console`.
- *Alternativa*: contraseñas de desarrollo fijas en el yml. Se descartó por la regla E3: nada que parezca una credencial se versiona.

### D10. Infraestructura de tests compartida (`src/test/java/ar/odonto/turnos/support`)
- `RelojDePrueba.en("2026-11-23T09:00")`: devuelve un `Clock` fijo en la zona AR.
- `DatosDePrueba`: *builders* con datos ficticios para cada entidad (paciente con DNI `0000000X`, profesional "Ana Ejemplo", etc.).
- Un controlador **solo de test** (en `src/test`) que lanza cada `CodigoError`, para verificar el manejador sin depender de endpoints de negocio.
- Slices de test: `@DataJpaTest` para las consultas y `@SpringBootTest` + MockMvc para el formato de errores y el health check.

## Risks / Trade-offs

- **[El modelo fijado ahora no le alcanza a un change de la ola 1]** → Antes del apply, el líder repasa con cada dueño la ficha de su change en `CHANGES.md`. Los ajustes posteriores van por un PR chico `chore(shared)` del líder (regla anti-colisión 8).
- **[Anotaciones JPA en el modelo acoplan el dominio a la persistencia]** → Se acepta por plazo. Las reglas (`domain/rules`) siguen siendo Java puro y son las que concentran el valor y los tests.
- **[H2 y PostgreSQL difieren en dialecto o constraints]** → Las consultas son JPQL estándar, sin SQL nativo. La migración a Postgres queda fuera del MVP.
- **[`start.spring.io` no disponible o versión 3.5.x distinta a la esperada]** → Se registra la versión exacta usada en el commit del apply. Si el servicio cae, se usa el último `pom.xml` conocido de la serie 3.5.
- **[Carrera: dos solicitudes que crean turnos a la vez (PA-06)]** → Fuera del alcance de este change. Los índices quedan listos y la estrategia (lock o aislamiento) la decide C-02.
- **[`JAVA_HOME` sin definir en Windows]** → El wrapper usa `java` del PATH. El README indica cómo definir `JAVA_HOME` igual.

## Migration Plan

No aplica: es el primer código del repositorio. El rollback es revertir el PR de `feat/scaffold-proyecto`.

## Notas de implementación (apply, 2026-11-20)

Desviaciones y precisiones respecto de las decisiones originales. Todas quedaron cubiertas por tests:

- **Spring Boot 4.2.0 en lugar de 3.x**: ver D1. El líder lo aprobó durante el apply.
- **`CodigoError` guarda el estado HTTP como `int`**, no como `HttpStatus` de Spring (D7). Así `domain/error` queda en Java puro y las reglas de C-02 y C-08 lo usan sin depender de Spring (regla J1).
- **Dos códigos agregados al catálogo**: `NO_AUTENTICADO` (401) y `ACCESO_DENEGADO` (403), para C-05 y para RN-AG-02 en C-07. Se actualizó la spec.
- **`Turno` no calcula su fin**: recibe `inicio` y `fin` y solo protege `fin > inicio` y el sillón obligatorio. El cálculo `fin = inicio + duración` es RN-TU-01 y pertenece a C-02. `marcarCancelado(...)` es la transición de estado que usará C-08; las condiciones de anticipación son de C-08.
- **Base H2 con nombre único por contexto** (`spring.datasource.generate-unique-name`): evita que los contextos de test compartan datos. El perfil `dev` fija `jdbc:h2:mem:turnos` para la consola H2.
- **Repositorios Spring Data como interfaces de primer nivel** (no anidadas): Spring Data no detecta repositorios anidados por defecto.
- **Seguridad permisiva** (D8): además de la cadena y de `UsuarioActual`, define un `UserDetailsService` vacío y reemplazable. Así Spring no genera un usuario en memoria con la contraseña impresa en el log.
- **Clases de test de Spring Boot 4 en paquetes por módulo**, para quienes escriban tests:
  - `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
  - `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
  - `org.springframework.boot.jpa.test.autoconfigure.TestEntityManager`
