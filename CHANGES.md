# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **Turnos Odontología** (agenda de turnos para consultorios odontológicos argentinos que rechaza choques por profesional y por sillón).
> Cada change es atómico: un integrante puede implementarlo en una sesión de trabajo (~4-6 horas).
> **Leer este archivo antes de invocar la skill de propuesta instalada.**

- Fecha de generación: 2026-11-20. Fecha de entrega: **2026-11-25**.
- Stack: Java 17, Spring Boot 4.2 (decidido en el apply de C-01; ver su design.md D1), Maven Wrapper, Spring Data JPA, H2, Thymeleaf, Spring Security, JUnit 5. Paquete base `ar.odonto.turnos` (`knowledge-base/02` y `08`).
- Equipo: 6 programadores. Líder y dueño del repo: **Santiago**. Integrantes: Joaquin, Romero, Castillo, Adriano, Valentin.
- Consigna académica: **un** change tiene que pasar por el ciclo OPSX completo (explore → propose → apply → verify → archive), con al menos 3 escenarios verificables (feliz, error de negocio, borde) y un test por escenario. El change evaluado es **C-02 `crear-turno-sin-solapamientos`** y lo lleva el líder.
- Política de diseño visual: `deferred`. El primer change que agrega pantallas (C-09) tiene que invocar antes la skill `design-system` (una sola vez para todo el proyecto).

---

## Cómo usar este documento

1. **Identificá tu change** en la tabla de asignación (sección "Asignación del equipo") y abrí tu rama `feat/<nombre-del-change>` desde `main` actualizado.
2. **Leé la KB** que figura en "Leer antes" de tu change, más `knowledge-base/05_reglas_de_negocio.md` si tu change toca reglas, y las reglas anti-colisión de este documento.
3. **Corré la propuesta**: invocá la skill de propuesta instalada (`openspec-propose`, o `openspec-explore` primero si hay dudas) con el nombre exacto del change. Las preguntas abiertas que bloquean tu change están listadas en su ficha. El estado de artefactos lo da siempre el CLI: `openspec status`, nunca se adivina.
4. **Implementá, verificá y archivá**: `openspec-apply-change`, después verificación (tests en verde con `./mvnw test`) y `openspec-archive-change`. Para C-02 el ciclo completo es obligatorio y lo audita el líder.
5. **Marcá el checkbox** del change en esta página (`[ ]` → `[x]`) en el mismo PR y abrí el PR a `main`. El líder revisa y mergea. Solo el líder edita este archivo fuera de ese checkbox.

---

## Árbol de dependencias

```
C-01 scaffold-proyecto                                   (Santiago, OLA 0, bloqueante)
 │
 ├── C-02 crear-turno-sin-solapamientos                  (Santiago, OLA 1, change evaluado, camino crítico)
 │     └── C-08 cancelar-turno                           (Romero, OLA 2, comparte la capability `turnos`)
 │
 ├── C-03 configurar-recursos                            (Joaquin, OLA 1)
 ├── C-04 gestionar-pacientes                            (Romero, OLA 1)
 ├── C-05 autenticacion-y-roles                          (Castillo, OLA 1)
 ├── C-06 link-whatsapp-turno                            (Adriano, OLA 1)
 └── C-07 agenda-del-dia                                 (Valentin, OLA 1)

 C-03 + C-04 + C-05 (+ gate de design-system)
 └── C-09 ui-base-y-gestion                              (Valentin, OLA 2)
       └── C-10 ui-turnos-y-agenda                       (Joaquin, OLA 2; además depende de C-02, C-06, C-07, C-08)

 C-01 ... C-10 (los que se hayan completado)
 └── C-11 integracion-y-verificacion-final               (Santiago, OLA 3, cierre)
```

Dependencias explícitas, de una línea:

| Change | Depende de |
|---|---|
| C-01 | ninguna |
| C-02, C-03, C-04, C-05, C-06, C-07 | C-01 |
| C-08 | C-02 |
| C-09 | C-03, C-04, C-05 |
| C-10 | C-09, C-02, C-06, C-07, C-08 |
| C-11 | todos los changes planificados que se hayan completado (mínimo C-02 y C-08) |

### Paralelismo por fase

```
GATE 0: C-01 scaffold-proyecto ✓ (mergeado a main)                  ← FORK (6 ramas en paralelo)
  → C-02 crear-turno-sin-solapamientos     [Santiago]
  → C-03 configurar-recursos               [Joaquin]
  → C-04 gestionar-pacientes               [Romero]
  → C-05 autenticacion-y-roles             [Castillo]
  → C-06 link-whatsapp-turno               [Adriano]
  → C-07 agenda-del-dia                    [Valentin]

GATE 1: C-02 ✓ (mergeado)
  → C-08 cancelar-turno                    [Romero — si C-04 ✓; reasignable]

GATE 2: C-03 ✓ + C-04 ✓ + C-05 ✓ (mergeados)
  → C-09 ui-base-y-gestion                 [Valentin — si C-07 ✓; reasignable]
     (antes, una sola vez: invocar la skill `design-system` y obtener aprobación del líder.
      La fundación puede prepararse desde el GATE 0 porque no depende de código.)

GATE 3: C-09 ✓ + C-02 ✓ + C-06 ✓ + C-07 ✓ + C-08 ✓
  → C-10 ui-turnos-y-agenda                [Joaquin — si C-03 ✓; reasignable]

GATE 4: todos los changes planificados ✓ (C-10 es recortable, ver abajo)
  → C-11 integracion-y-verificacion-final  [Santiago]
```

Reglas de paralelismo:
- Los 6 changes de la ola 1 dependen **solo** de C-01 y no se bloquean entre sí. Cada uno tiene su propia capability OpenSpec y su propio paquete (ver reglas anti-colisión).
- Durante el GATE 0 los integrantes pueden adelantar `explore` y `propose` en su rama (los artefactos viven en `openspec/changes/<nombre>/`, un directorio por change) sin esperar el merge del scaffold. Solo el `apply` necesita el scaffold mergeado.
- La UI (C-09, C-10) es recortable: la consigna acepta API o módulo de dominio sin UI obligatoria. Si se atrasa el cronograma, se corta la UI y se entrega la API completa.

### Camino crítico (4 changes — mínimo irreducible)

`C-01 scaffold-proyecto` → `C-02 crear-turno-sin-solapamientos` → `C-08 cancelar-turno` → `C-11 integracion-y-verificacion-final`

- El mínimo para la entrega académica es hasta C-02 archivado (más C-11 para verificar).
- Con UI incluida, el camino más largo es `C-01 → C-03/C-04/C-05 → C-09 → C-10 → C-11`* (marcado con asterisco: es el otro posible "último" camino; es recortable).

### Plan óptimo con 6 integrantes (reemplaza el plan de 3 agentes)

| Paso (día) | Santiago (líder) | Joaquin | Romero | Castillo | Adriano | Valentin |
|---|---|---|---|---|---|---|
| 0 (vie 20) | C-01 explore+propose+apply, PR | Instalar JDK 17; explore+propose C-03 | Instalar JDK 17; explore+propose C-04 | Instalar JDK 17; explore+propose C-05 (decidir IN-01) | Instalar JDK 17; explore+propose C-06 | Instalar JDK 17; explore+propose C-07 |
| 1 (sáb 21) | Merge C-01 (GATE 0); explore+propose C-02, decidir PA-03/04/06/07; arrancar apply | apply C-03 | apply C-04 | apply C-05 | apply C-06 | apply C-07 |
| 2 (dom 22) | apply C-02; revisa PRs de ola 1 | verify + PR C-03; verificar fuente #1 | verify + PR C-04; verificar fuente #2; explore+propose C-08 | apply/verify C-05 | verify + PR C-06; verificar fuente #4 | verify + PR C-07; prepara fundación `design-system` |
| 3 (lun 23) | verify + archive C-02 (GATE 1); merge de PRs | explore+propose C-10; revisión cruzada de PRs | apply/verify/archive C-08 (al mergearse C-02) | PR C-05 (si no salió); revisión de seguridad | revisión cruzada de PRs; apoyo en tests | C-09 (GATE 2) |
| 4 (mar 24) | C-11 integración; video; consolidar reflexión | C-10 (GATE 3) | reflexión; apoyo a C-10 | reflexión; chequeo final de RBAC | guion del video; reflexión | PR C-09 / apoyo a C-10 |
| 5 (mié 25) | Entrega | — | — | — | — | — |

Los changes de ola 2 están repartidos para que **nadie tenga más de 2 changes**: C-08 → Romero, C-09 → Valentin, C-10 → Joaquin. Adriano (C-06) y Castillo (C-05) quedan como revisores cruzados de PRs y apoyo en tests. **La asignación es re-asignable**: si otro integrante queda libre antes, el líder la cambia en la tabla de asignación.

Carga por integrante: Santiago C-01, C-02, C-11 (líder) · Joaquin C-03, C-10 · Romero C-04, C-08 · Castillo C-05 · Adriano C-06 · Valentin C-07, C-09.

---

## Asignación del equipo

| Change | Owner | Ola | Rama | Capability OpenSpec | Archivos / paquetes que posee (bajo `ar.odonto.turnos` y `src/test/java/ar/odonto/turnos`) |
|---|---|---|---|---|---|
| C-01 `scaffold-proyecto` | Santiago | 0 | `feat/scaffold-proyecto` | `base-del-sistema` | Todo lo compartido: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/**`, `TurnosApplication`, `domain/model/**`, `domain/port/**`, `domain/error/**`, `infrastructure/persistence/**`, `infrastructure/config/**`, `web/api/error/**`, `src/main/resources/application*.yml`, `README.md`, `src/test/.../support/**`, `.gitignore` |
| C-02 `crear-turno-sin-solapamientos` | Santiago | 1 | `feat/crear-turno-sin-solapamientos` | `turnos` | `domain/rules/turnos/**`, `application/turnos/**`, `web/api/turnos/**`; tests en `domain/rules/turnos/**`, `application/turnos/**`, `web/api/turnos/**` |
| C-03 `configurar-recursos` | Joaquin | 1 | `feat/configurar-recursos` | `configuracion-consultorio` | `application/configuracion/**`, `web/api/configuracion/**`; tests espejo |
| C-04 `gestionar-pacientes` | Romero | 1 | `feat/gestionar-pacientes` | `pacientes` | `application/pacientes/**`, `web/api/pacientes/**`; tests espejo |
| C-05 `autenticacion-y-roles` | Castillo | 1 | `feat/autenticacion-y-roles` | `acceso` | `infrastructure/security/**`, `application/acceso/**`, `web/api/auth/**`; tests espejo |
| C-06 `link-whatsapp-turno` | Adriano | 1 | `feat/link-whatsapp-turno` | `aviso-whatsapp` | `infrastructure/whatsapp/**`, `application/aviso/**`, `web/api/aviso/**`; tests espejo |
| C-07 `agenda-del-dia` | Valentin | 1 | `feat/agenda-del-dia` | `agenda` | `application/agenda/**`, `web/api/agenda/**`; tests espejo |
| C-08 `cancelar-turno` | Romero | 2 | `feat/cancelar-turno` | `turnos` (delta sobre la spec archivada de C-02) | `domain/rules/cancelacion/**`, `application/cancelacion/**`, `web/api/cancelacion/**`; tests espejo |
| C-09 `ui-base-y-gestion` | Valentin | 2 | `feat/ui-base-y-gestion` | `interfaz-gestion` | `design-system/**`, `web/view/gestion/**`, `src/main/resources/templates/layout/**`, `templates/login.html`, `templates/gestion/**`, `static/**` |
| C-10 `ui-turnos-y-agenda` | Joaquin | 2 | `feat/ui-turnos-y-agenda` | `interfaz-turnos` | `web/view/turnos/**`, `templates/turnos/**`, `templates/agenda/**` |
| C-11 `integracion-y-verificacion-final` | Santiago | 3 | `feat/integracion-final` | ninguna (checklist, sin propose OPSX) | `README.md` (ajustes finales), `CHANGES.md` (checkboxes), `docs/**` |

> Asignación por defecto. Los owners de la ola 2 son re-asignables por el líder.

---

## Reglas anti-colisión

Objetivo: que los 6 changes de la ola 1 se puedan desarrollar y mergear en cualquier orden sin conflictos.

1. **Archivos compartidos = solo C-01.** Son de C-01 y nadie más los edita en las olas 1 y 2: `pom.xml`, `mvnw*`, `.mvn/`, `application.yml`, `application-dev.yml`, `TurnosApplication`, todo `domain/model/**`, `domain/port/**`, `domain/error/**` (incluido `CodigoError`), `infrastructure/persistence/**`, `infrastructure/config/**` (Clock, seed, seguridad permisiva), `web/api/error/**` (manejador global), `src/test/.../support/**` y `README.md`. C-01 ya trae todas las dependencias Maven (web, validation, data-jpa, security, thymeleaf, actuator, h2, test, spring-security-test, thymeleaf-extras-springsecurity6) y todas las propiedades `turnos.*`.
2. **Cada change agrega código solo en su propio paquete**, según la tabla de asignación. La estructura en capas de la KB (`domain`, `application`, `infrastructure`, `web`) se respeta, pero con **un subpaquete por feature** dentro de cada capa (por ejemplo `application/pacientes/`, `web/api/pacientes/`). Así dos changes nunca tocan el mismo archivo ni el mismo directorio.
3. **Un change = una capability OpenSpec.** Cada uno crea su propio `openspec/specs/<capability>/` al archivar, así `openspec archive` no genera conflictos. La única excepción es C-08, que modifica la capability `turnos` y por eso **depende de C-02** y se archiva después.
4. **Repositorios y queries**: C-01 define en cada puerto todas las consultas que la ola 1 necesita (choques por profesional/sillón/paciente, turnos activos por profesional y rango de fechas, existencia por matrícula/nombre/DNI, horario por día). Si un change necesita una query distinta, la declara en una interfaz propia dentro de su paquete; **no** edita el repositorio de C-01.
5. **Códigos de error**: `CodigoError` ya trae todos los códigos de `05_reglas_de_negocio.md` más los de validación de configuración y datos. Si un change necesita un código nuevo, no lo agrega: lo pide al líder.
6. **Seguridad desacoplada**: C-01 deja una configuración permisiva (`SecurityFilterChain` y `UsuarioActual`/`Solicitante` con valores por defecto, marcados como reemplazables con `@ConditionalOnMissingBean`). C-05 define sus propios beans en `infrastructure/security/` y los de C-01 se retiran solos, sin editar nada compartido. Los permisos por rol se aplican por ruta en la cadena de C-05 (matriz de `03_actores_y_roles.md`); los controladores de la ola 1 no llevan anotaciones de seguridad. La regla RN-AG-02 (el odontólogo solo ve su agenda) se resuelve en el caso de uso de C-07 con un `Solicitante` recibido como parámetro, testeable sin Spring Security.
7. **Datos de prueba**: el seed de C-01 corre solo con el perfil `dev`. Los tests usan el perfil por defecto con base vacía y arman sus propios datos ficticios con fixtures de su propio paquete de tests (nunca datos reales, RN-DP-01).
8. **Si un change necesita editar un archivo compartido**, el integrante no lo toca: le pasa al líder el cambio exacto (archivo, motivo y diff propuesto). El líder lo aplica en un PR chico `chore(shared): ...` a `main`, y los demás rebasean.
9. **Este archivo (`CHANGES.md`)**: cada integrante modifica solo el checkbox de su change.

---

## Orden de merge y protocolo de PR (líder)

**Orden de merge**
1. `C-01 scaffold-proyecto` se mergea **primero**. Hasta que no esté en `main`, no se mergea nada más. Al mergearlo, el líder avisa al equipo (GATE 0).
2. Los PRs de la ola 1 (C-02 a C-07) se mergean **en cualquier orden**, siempre después de que su autor haga `git fetch origin && git rebase origin/main` y vuelva a correr los tests.
3. La ola 2 se mergea cuando sus dependencias ya están en `main`: C-08 después de C-02; C-09 después de C-03, C-04 y C-05; C-10 después de C-09, C-02, C-06, C-07 y C-08.
4. `C-11` es lo último.

**Protocolo de PR**
- Cada integrante trabaja en su rama `feat/<nombre-del-change>` y abre un PR a `main`. Título: `feat(<capability>): <resumen>`.
- Requisitos para que el líder revise: `./mvnw test` en verde (en Windows, `mvnw.cmd test`) después del rebase sobre `main`; `openspec status` del change sin artefactos pendientes; checkbox propio marcado en `CHANGES.md`; al menos un test por escenario de su spec.
- Chequeo de colisiones antes de mergear: `git diff --name-only origin/main...HEAD` solo debe listar rutas del change (las de la tabla de asignación) y el directorio `openspec/changes/<nombre>/` o su versión archivada. Si aparece un archivo compartido, el PR vuelve al autor.
- Chequeo de contenido: sin secretos, sin datos reales de pacientes, sin credenciales (RN-DP-01; `.gitignore` cubre `.env` y `target/`).
- Estrategia de merge sugerida: *squash and merge* para dejar un commit por change en `main`.
- Conflictos: los resuelve el autor del PR rebaseando; el líder no resuelve semántica ajena.
- Archivo de los changes: cada owner corre `openspec-archive-change` como último paso de su rama una vez verificado. **C-02 lo archiva y lo audita el líder.** Si un archive genera conflicto en `openspec/`, lo resuelve el líder.
- En Windows, el wrapper `mvnw` tiene que quedar ejecutable en el repo (`git update-index --chmod=+x mvnw`); lo hace C-01.

---

## FASE 1 — Fundación

> Un solo change, bloqueante. Es dueño de todos los archivos compartidos para que la ola 1 no colisione.

### [C-01] `scaffold-proyecto`
- **Estado**: `[x]` completado y archivado el 2026-11-20 (`openspec/changes/archive/2026-11-20-scaffold-proyecto`, spec `base-del-sistema`). Spring Boot 4.2, ver su design D1.
- **Owner / rama**: Santiago / `feat/scaffold-proyecto`
- **Capability**: `base-del-sistema`
- **Scope**:
  - `pom.xml` Spring Boot 4.2 / Java 17 con **todas** las dependencias: web, validation, data-jpa, security, thymeleaf, actuator, h2, test, spring-security-test, thymeleaf-extras-springsecurity6. Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`), `mvnw` con bit ejecutable.
  - `TurnosApplication` en `ar.odonto.turnos`. Estructura de capas `domain` / `application` / `infrastructure` / `web` según `08_arquitectura_propuesta.md`.
  - Entidades JPA y sus repositorios (puertos en `domain/port`, adaptadores Spring Data en `infrastructure/persistence`): `Consultorio`, `HorarioAtencion`, `Profesional`, `Sillon`, `Prestacion`, `Paciente`, `Turno` (con `TurnoEstado` `RESERVADO`/`CANCELADO`, índices `(profesionalId, inicio)`, `(sillonId, inicio)`, `(pacienteId, inicio)`), `Usuario` (con `Rol` `ADMIN`/`SECRETARIA`/`ODONTOLOGO`). Constraints de `04_modelo_de_datos.md` (únicos, `apertura < cierre`, `fin > inicio`).
  - Queries de repositorio que usa la ola 1: choques con margen por profesional, por sillón y por paciente (superposición simple) sobre turnos `RESERVADO`; turnos activos por profesional y rango de fechas; `existsByMatricula`, `existsByNombre`, `existsByDni`; horario por consultorio y día de la semana.
  - Decisión de mapeo (anotaciones JPA en el modelo vs. mapeo separado, DD-03) cerrada en el `design.md` de este change y fija para todos.
  - `CodigoError` con **todos** los códigos de `05_reglas_de_negocio.md` (`CHOQUE_PROFESIONAL`, `CHOQUE_SILLON`, `SILLON_REQUERIDO`, `CHOQUE_PACIENTE`, `TURNO_EN_PASADO`, `FUERA_DE_HORARIO`, `RECURSO_INEXISTENTE`, `RECURSO_INACTIVO`, `CANCELACION_TARDIA`, `TURNO_YA_CANCELADO`, `PACIENTE_SIN_TELEFONO`) con su HTTP asociado, más los de validación de configuración y datos (duración inválida, horario inválido, dato duplicado).
  - `ReglaDeNegocioException(codigo)` y manejador global `@RestControllerAdvice` que devuelve Problem Details (RFC 7807) con el campo `codigo` (409 choques, 422 reglas, 404 inexistente, 400 entrada mal formada).
  - Bean `Clock` inyectable con zona `America/Argentina/Buenos_Aires` (`turnos.zona-horaria`) y propiedades `turnos.margen-minutos=5`, `turnos.cancelacion-horas-minimas=6`.
  - H2 en memoria (`application.yml`); `application-dev.yml` con consola H2 y seed ficticio: Consultorio Demo L-V 08:00-20:00, Sillón 1 y 2, Ana Ejemplo y Bruno Prueba, prestaciones Control 30 / Limpieza 45 / Conducto 60 / Extracción 40, 3 pacientes ficticios, usuarios `admin`, `secretaria`, `ana`. Contraseñas de seed desde `SEED_ADMIN_PASSWORD` u otras variables de entorno; `PasswordEncoder` BCrypt.
  - Actuator `/actuator/health` expuesto.
  - Seguridad permisiva de reemplazo: `SecurityFilterChain` que permite todo y `UsuarioActual`/`Solicitante` por defecto, ambos con `@ConditionalOnMissingBean`.
  - `src/test/.../support/`: helpers de `Clock.fixed` y builders base.
  - `README.md`: requisitos (JDK 17), cómo correr con `./mvnw spring-boot:run`, cómo correr los tests (`./mvnw test` / `mvnw.cmd test`), nota de `src/test/java` (DD-07), reglas del flujo de ramas y PRs.
  - Tests: arranque del contexto, repositorios (cada query de choque con casos de borde), manejador global (código y HTTP por cada `CodigoError`), `/actuator/health` responde 200.
- **Dependencias**: ninguna
- **Governance**: CRITICO (todo lo demás referencia estos modelos y archivos compartidos)
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` (todas las entidades y el seed)
  - `knowledge-base/05_reglas_de_negocio.md` (Definiciones y todos los códigos de error)
  - `knowledge-base/08_arquitectura_propuesta.md` (estructura de directorios, variables de entorno, testing)
  - `knowledge-base/09_decisiones_y_supuestos.md` (DD-02, DD-03, DD-04, DD-07)
  - `knowledge-base/10_preguntas_abiertas.md` (PA-01: instalar JDK 17 antes de arrancar)

---

## FASE 2 — Núcleo y servicios en paralelo

> Los 6 changes dependen solo de C-01. Cada uno en su capability y su paquete. El líder lleva C-02 (el evaluado).

### [C-02] `crear-turno-sin-solapamientos`
- **Estado**: `[x]` completado y archivado el 2026-11-20 (`openspec/changes/archive/2026-11-20-crear-turno-sin-solapamientos`, spec `turnos`). Abre el GATE 1 (C-08).
- **Owner / rama**: Santiago / `feat/crear-turno-sin-solapamientos`
- **Capability**: `turnos`
- **Scope**:
  - Historia US-010; reglas RN-TU-01 a RN-TU-10, RN-GL-01, RN-GL-02.
  - Dominio puro (sin Spring) en `domain/rules/turnos/`: `Intervalo` (value object: `[inicio, fin)`, `chocaConMargen`, `seSuperpone`) y `ValidadorDeTurno` que aplica el orden RN-TU-09: recursos (08) → pasado (06) → horario (07) → sillón (03) → profesional (02, salvo sobreturno 04) → paciente (05).
  - Caso de uso `CrearTurno` en `application/turnos/`, `@Transactional`: carga y valida recursos activos, calcula `fin = inicio + duración` (RN-TU-01, se ignora el `fin` del cliente), valida contra el `Clock` y el `HorarioAtencion`, consulta choques y persiste en estado `RESERVADO` con `creadoEn` y `creadoPor`.
  - Concurrencia (RN-TU-10, PA-06): validación y persistencia en la misma transacción; decidir en `design.md` entre lock pesimista (sobre sillón y profesional), aislamiento serializable o revalidación; incluir un test con dos creaciones concurrentes.
  - `POST /api/turnos` → 201 con el turno (incluye `fin`); `GET /api/turnos/{id}` (detalle). DTOs con Bean Validation. Errores con `codigo` según `CodigoError`.
  - **Escenarios verificables (mínimo 3, un test por escenario)**:
    - Feliz: turno de 30 min (Control) en sillón y profesional libres, se persiste `RESERVADO` con `fin = inicio + 30`.
    - Error de negocio: turno que choca con el sillón ocupado → `CHOQUE_SILLON` 409; mismo profesional ocupado → `CHOQUE_PROFESIONAL` 409; paciente superpuesto → `CHOQUE_PACIENTE`.
    - Borde: existente 09:30-10:00; nuevo a las 10:05 se acepta y a las 10:04 se rechaza (margen de 5 min).
    - Otros escenarios del spec: sobreturno con profesional ocupado y otro sillón se acepta; sobreturno con el sillón ocupado se rechaza; turno en el pasado → `TURNO_EN_PASADO` 422; fuera de horario (empieza antes de abrir o termina después de cerrar) → `FUERA_DE_HORARIO` 422; un turno `CANCELADO` (estado fijado desde el fixture) no bloquea el horario; recurso inactivo → `RECURSO_INACTIVO`; orden de validación RN-TU-09.
  - Decisiones previas en `explore`: PA-03 (¿el margen aplica entre turno y su sobreturno con el mismo profesional? supuesto SU-04: no), PA-04 (quién marca sobreturno), PA-07 (múltiplos de 5 o cualquier minuto), PA-06 (concurrencia).
  - Tests: unitarios de dominio con `Clock.fixed`, de caso de uso y de API (`@SpringBootTest` + MockMvc sobre H2).
- **Dependencias**: `C-01`
- **Governance**: ALTO (reglas núcleo del producto y change evaluado; ciclo OPSX completo, incluido archive)
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §Definiciones y §RN-TU
  - `knowledge-base/06_funcionalidades.md` §US-010
  - `knowledge-base/07_flujos_principales.md` §Flujo 1 (casos de error y ejemplos de borde)
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones y §Testing
  - `knowledge-base/10_preguntas_abiertas.md` §PA-03, PA-04, PA-06, PA-07

### [C-03] `configurar-recursos`
- **Estado**: `[x]` completado y archivado el 2026-11-21 (`openspec/changes/archive/2026-11-21-configurar-recursos`, spec `configuracion-consultorio`)
- **Owner / rama**: Joaquin / `feat/configurar-recursos`
- **Capability**: `configuracion-consultorio`
- **Scope**:
  - Historias US-001 a US-004; reglas RN-CF-01 a RN-CF-04.
  - Casos de uso en `application/configuracion/`: alta, edición, listado y baja lógica (`activo = false`) de `Profesional` (matrícula única, vinculable a usuario), `Sillon` (nombre único) y `Prestacion` (duración > 0 y múltiplo de 5); lectura y reemplazo del horario de atención por día (`apertura < cierre`, una franja por día, días sin franja = cerrados).
  - Endpoints en `web/api/configuracion/`: `GET/POST/PUT /api/profesionales`, `GET/POST/PUT /api/sillones`, `GET/POST/PUT /api/prestaciones`, `GET/PUT /api/consultorio/horario`. Baja lógica por `PUT` con `activo=false`. Un recurso con turnos nunca se borra físicamente (RN-CF-03).
  - **Escenarios (un test cada uno)**: feliz (alta de prestación Control 30 min); error de negocio (duración 7 o 0 rechazada, matrícula duplicada rechazada); borde (desactivar un sillón con turnos conserva los turnos; horario con `apertura == cierre` rechazado).
  - Tests: unitarios de validación, API con MockMvc sobre H2. Datos siempre ficticios.
- **Dependencias**: `C-01`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Consultorio, §HorarioAtencion, §Profesional, §Sillon, §Prestacion
  - `knowledge-base/05_reglas_de_negocio.md` §RN-CF
  - `knowledge-base/06_funcionalidades.md` §Épica 1
  - `knowledge-base/02_descripcion_general.md` §API REST
  - `knowledge-base/10_preguntas_abiertas.md` §PA-02

### [C-04] `gestionar-pacientes`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Romero / `feat/gestionar-pacientes`
- **Capability**: `pacientes`
- **Scope**:
  - Historia US-050; reglas RN-DP-01 a RN-DP-03.
  - Casos de uso en `application/pacientes/`: alta, edición y listado/búsqueda de `Paciente` (nombre, apellido, DNI único, teléfono E.164 sin `+`, email opcional).
  - Endpoints en `web/api/pacientes/`: `GET/POST/PUT /api/pacientes`, `GET /api/pacientes/{id}`.
  - Validaciones: DNI único (código de dato duplicado), teléfono en formato internacional, campos obligatorios con Bean Validation.
  - RN-DP-03: los logs de la capa no imprimen DNI, teléfono ni nombre completo (test que lo verifica con captura de logs).
  - **Escenarios (un test cada uno)**: feliz (alta con datos ficticios); error de negocio (DNI duplicado rechazado); borde (teléfono con `+` o espacios rechazado; alta sin teléfono aceptada, porque el teléfono es opcional para el alta pero bloquea el link en C-06).
  - Todos los ejemplos y fixtures con datos ficticios.
- **Dependencias**: `C-01`
- **Governance**: MEDIO (datos personales, Ley 25.326)
- **Leer antes**:
  - `knowledge-base/04_modelo_de_datos.md` §Paciente
  - `knowledge-base/05_reglas_de_negocio.md` §RN-DP
  - `knowledge-base/06_funcionalidades.md` §US-050
  - `knowledge-base/03_actores_y_roles.md` §RBAC (pacientes)

### [C-05] `autenticacion-y-roles`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Castillo / `feat/autenticacion-y-roles`
- **Capability**: `acceso`
- **Scope**:
  - Historias US-040 y US-041; reglas RN-AU-01 a RN-AU-04.
  - Resolver **IN-01** (sesión vs JWT) en `design.md`; propuesta de la KB: form login con sesión de Spring Security para las vistas y JWT solo para `/api/**` si se expone a otro cliente. Registrar la decisión.
  - `infrastructure/security/`: `SecurityFilterChain` propio (reemplaza al permisivo de C-01), `UserDetailsService` sobre `Usuario` (hash BCrypt), implementación de `UsuarioActual`/`Solicitante` con el rol y el `profesionalId` vinculado, matriz RBAC por ruta según `03_actores_y_roles.md`, rutas públicas (`/login`, `/api/auth/login`, estáticos, `/actuator/health`, `/h2-console` solo con perfil `dev`).
  - `web/api/auth/`: `POST /api/auth/login` (credenciales inválidas → 401; sin permiso → 403). El `/login` es la página de login por defecto de Spring Security; la vista Thymeleaf propia se hace en C-09 (así la ola 1 no agrega pantallas y no dispara el gate de design-system).
  - RN-AU-04: un usuario `ODONTOLOGO` sin `Profesional` vinculado se rechaza.
  - **Escenarios (un test cada uno)**: feliz (login correcto de `secretaria` y acceso a ruta permitida); error de negocio (contraseña incorrecta → 401; `ODONTOLOGO` pidiendo una ruta de `ADMIN` → 403); borde (acceso sin autenticar a una ruta protegida; ruta pública accesible sin login; contraseña nunca en logs ni en texto plano).
  - Tests con `spring-security-test` (`@WithMockUser`, `httpBasic` / `formLogin`). Contraseñas de seed y secretos solo por variables de entorno.
- **Dependencias**: `C-01`
- **Governance**: CRITICO (auth y roles; solo análisis y propuesta antes de escribir código, el líder revisa el `design.md`)
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md` (RBAC y rutas públicas)
  - `knowledge-base/05_reglas_de_negocio.md` §RN-AU y §RN-DP-02
  - `knowledge-base/08_arquitectura_propuesta.md` §Seguridad y §Variables de entorno
  - `knowledge-base/10_preguntas_abiertas.md` §IN-01 y PA-09
  - `knowledge-base/06_funcionalidades.md` §Épica 5

### [C-06] `link-whatsapp-turno`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Adriano / `feat/link-whatsapp-turno`
- **Capability**: `aviso-whatsapp`
- **Scope**:
  - Historia US-030; reglas RN-WA-01 a RN-WA-04; decisión DD-09 (solo `wa.me`, sin API).
  - Puerto `NotificadorDeTurnos` (declarado por C-01 en `domain/port`) con implementación MVP `WaMeLinkGenerator` en `infrastructure/whatsapp/`: arma `https://wa.me/{telefono}?text={mensaje URL-encoded}` con el teléfono en formato internacional sin `+` ni espacios.
  - Caso de uso `GenerarLinkWhatsApp` en `application/aviso/`: arma el mensaje con fecha, hora, profesional y prestación, sin datos clínicos. Sin teléfono → `PACIENTE_SIN_TELEFONO` (422). El sistema no envía nada (RN-WA-04).
  - Endpoint `GET /api/turnos/{id}/whatsapp-link` en `web/api/aviso/` (controlador propio; no edita el de C-02). Turno inexistente → 404.
  - **Escenarios (un test cada uno)**: feliz (link con mensaje URL-encoded correcto para un turno ficticio); error de negocio (paciente sin teléfono → `PACIENTE_SIN_TELEFONO`); borde (caracteres con tildes, espacios y `ñ` correctamente codificados; teléfono con `+` o espacios normalizado).
  - Los tests arman el turno directamente con repositorios de C-01 (no dependen de C-02).
- **Dependencias**: `C-01`
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §RN-WA
  - `knowledge-base/06_funcionalidades.md` §US-030
  - `knowledge-base/07_flujos_principales.md` §Flujo 4
  - `knowledge-base/09_decisiones_y_supuestos.md` §DD-09
  - `knowledge-base/08_arquitectura_propuesta.md` §Patrones (puerto de notificación)

### [C-07] `agenda-del-dia`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Valentin / `feat/agenda-del-dia`
- **Capability**: `agenda`
- **Scope**:
  - Historia US-020; reglas RN-AG-01 y RN-AG-02; flujo 3.
  - Caso de uso `ConsultarAgenda(profesionalId, fecha, solicitante)` en `application/agenda/`: devuelve los turnos `RESERVADO` del profesional cuyo `inicio` cae en la fecha (zona del consultorio), ordenados por `inicio`, con paciente, prestación, sillón, horario y marca de sobreturno.
  - RN-AG-02 resuelta en el caso de uso con el `Solicitante` (rol y profesional vinculado) recibido como parámetro: un `ODONTOLOGO` solo ve su agenda; `SECRETARIA` y `ADMIN` ven la de cualquiera. Si no es el propio, error de acceso (403 mapeado desde la excepción). La conexión real con el usuario autenticado llega con C-05 sin tocar este código (ver regla anti-colisión 6).
  - Endpoint `GET /api/agenda?profesionalId=X&fecha=AAAA-MM-DD` en `web/api/agenda/`. Usa la query de turnos activos por profesional y rango definida en C-01.
  - **Escenarios (un test cada uno)**: feliz (3 turnos del día, uno `CANCELADO`: devuelve 2 ordenados por hora); error de negocio (un odontólogo pide la agenda de otro → denegado); borde (turno a las 23:59 del día anterior y a las 00:00 del siguiente quedan fuera; día sin turnos devuelve lista vacía; sobreturno marcado).
  - Tests con `Clock.fixed` y fixtures ficticios armados con repositorios de C-01 (no dependen de C-02).
- **Dependencias**: `C-01`
- **Governance**: MEDIO (control de acceso por propietario sobre datos de pacientes)
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §RN-AG
  - `knowledge-base/06_funcionalidades.md` §US-020
  - `knowledge-base/07_flujos_principales.md` §Flujo 3
  - `knowledge-base/03_actores_y_roles.md` §RBAC (agenda del día)
  - `knowledge-base/04_modelo_de_datos.md` §Turno

---

## FASE 3 — Cancelación e interfaz

> Se arrancan cuando sus dependencias están en `main`. Los owners son los por defecto y se pueden reasignar.

### [C-08] `cancelar-turno`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Romero / `feat/cancelar-turno`
- **Capability**: `turnos` (delta ADDED/MODIFIED sobre la spec archivada de C-02)
- **Scope**:
  - Historia US-011; reglas RN-CA-01 a RN-CA-04; flujo 2.
  - Caso de uso `CancelarTurno` en `application/cancelacion/` y reglas en `domain/rules/cancelacion/`: turno inexistente → 404; ya cancelado → `TURNO_YA_CANCELADO` (422); si `ahora > inicio - 6h` (`turnos.cancelacion-horas-minimas`, `Clock`) → `CANCELACION_TARDIA` (422); si no, estado `CANCELADO` y `canceladoEn`. Sin borrado físico (RN-CA-04).
  - Endpoint `POST /api/turnos/{id}/cancelacion` en `web/api/cancelacion/` (controlador propio, no edita el de C-02) → 200 con el turno actualizado.
  - **Prueba de que libera el horario (RN-CA-02)**: test de integración que crea un turno con el caso de uso de C-02, lo cancela, y vuelve a crear otro turno en el mismo horario, sillón y profesional, que ahora se acepta.
  - **Escenarios (un test cada uno)**: feliz (cancela con más de 6 h y el horario queda libre); error de negocio (menos de 6 h → `CANCELACION_TARDIA`; segunda cancelación → `TURNO_YA_CANCELADO`); borde (exactamente 6 h antes se acepta, 5 h 59 min se rechaza, con `Clock.fixed`).
  - Archivar **después** de que C-02 esté archivado, para que el delta de la capability `turnos` se aplique sobre su spec.
- **Dependencias**: `C-02`
- **Governance**: MEDIO
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` §RN-CA y RN-GL-02
  - `knowledge-base/06_funcionalidades.md` §US-011
  - `knowledge-base/07_flujos_principales.md` §Flujo 2
  - `openspec/specs/turnos/spec.md` (disponible cuando C-02 esté archivado)

### [C-09] `ui-base-y-gestion`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Valentin / `feat/ui-base-y-gestion`
- **Capability**: `interfaz-gestion`
- **Scope**:
  - **Paso previo obligatorio**: política visual `deferred`; antes de crear pantallas, invocar **una sola vez** la skill `design-system` (fundación en `design-system/`: `README.md`, `brand.md`, `brand.js`, `tokens.css`, `components.html`, `assets/`, `previews/`), con aprobación explícita del líder (`Approval: approved`). Leer `visual_design.mode` en `.active-orchestrator-state.json`. La fundación puede prepararse apenas esté C-01 mergeado.
  - Si la política resultante es `system-and-mockups`, mockups HTML en `openspec/changes/ui-base-y-gestion/mockups/` y aprobación antes del `apply`.
  - Layout base Thymeleaf (`templates/layout/`), `static/css`, vista `/login` propia (reemplaza la de Spring Security, usa el form login de C-05), pantallas de gestión en `web/view/gestion/`: profesionales, sillones, prestaciones, horario y pacientes (consumen los casos de uso de C-03 y C-04).
  - Visibilidad por rol con `thymeleaf-extras-springsecurity6` (la secretaria no ve configuración, el odontólogo no ve pacientes ajenos).
  - Primer PR chico con el layout y la fundación, para que C-10 pueda rebasear temprano.
  - **Escenarios (un test cada uno)**: feliz (login y alta de paciente por la vista); error de negocio (alta con DNI duplicado muestra el error de la regla); borde (usuario sin permiso redirigido o 403 en una pantalla de gestión).
  - Tests con MockMvc sobre vistas y, si hay Playwright MCP, capturas 390x844 y 1440x900 en `design-system/previews/`.
- **Dependencias**: `C-03, C-04, C-05`
- **Governance**: MEDIO (recortable si el cronograma se atrasa)
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios (`web/view`, `templates`)
  - `knowledge-base/03_actores_y_roles.md` (RBAC y rutas públicas)
  - `knowledge-base/06_funcionalidades.md` §Épicas 1, 5 y 6
  - `knowledge-base/09_decisiones_y_supuestos.md` §DD-05
  - `design-system/README.md` (se crea en este change: invocar antes la skill `design-system`)

### [C-10] `ui-turnos-y-agenda`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Joaquin / `feat/ui-turnos-y-agenda`
- **Capability**: `interfaz-turnos`
- **Scope**:
  - Reutiliza el layout y la fundación de `design-system/` aprobada en C-09 (misma revisión; si cambia un token, el líder revisa).
  - Pantallas en `web/view/turnos/`: formulario de crear turno (paciente, profesional, sillón, prestación, inicio, sobreturno) que muestra el `codigo` y mensaje de cada error de regla; listado/detalle con botón cancelar; botón de link de WhatsApp; vista "Mi agenda" del día para el odontólogo y selector de profesional para secretaria/admin. Plantillas en `templates/turnos/` y `templates/agenda/`.
  - Consume los casos de uso de C-02, C-06, C-07 y C-08 (no los modifica).
  - **Escenarios (un test cada uno)**: feliz (crear turno desde la vista y verlo en la agenda); error de negocio (choque de sillón se muestra con `CHOQUE_SILLON`); borde (cancelación tardía muestra `CANCELACION_TARDIA`; paciente sin teléfono oculta/deshabilita el link).
  - Verificación visual contra el mockup aprobado si la política es `system-and-mockups`.
- **Dependencias**: `C-09, C-02, C-06, C-07, C-08`
- **Governance**: MEDIO (recortable si el cronograma se atrasa)
- **Leer antes**:
  - `knowledge-base/07_flujos_principales.md` §Flujos 1 a 4
  - `knowledge-base/08_arquitectura_propuesta.md` §Estructura de directorios
  - `knowledge-base/03_actores_y_roles.md` (RBAC de turnos, agenda y WhatsApp)
  - `design-system/README.md` y `design-system/components.html` (creados en C-09)

---

## FASE 4 — Cierre

### [C-11] `integracion-y-verificacion-final`
- **Estado**: `[ ]` pendiente
- **Owner / rama**: Santiago / `feat/integracion-final`
- **Capability**: ninguna (checklist de cierre, sin propose OPSX)
- **Scope**:
  - `./mvnw test` completo en `main` actualizado, verde (en una máquina limpia, con JDK 17 y sin Maven instalado).
  - Arrancar con `./mvnw spring-boot:run` perfil `dev`; `GET /actuator/health` responde `UP`; recorrido manual con los usuarios de seed: crear turno, ver choque por sillón y por profesional, cancelar, agenda, link de WhatsApp.
  - README reproducible: un tercero clona, corre los tests y levanta la app siguiendo solo el README.
  - Revisión de que `openspec/specs/` tiene las capabilities esperadas, `CHANGES.md` con checkboxes al día, sin secretos ni datos reales en el repo.
  - Entregables de la cátedra: informe de Discovery con la sección "Verificación de fuentes" completa, video, reflexión escrita (ver plan día a día).
- **Dependencias**: todos los changes planificados completados (mínimo `C-02` y `C-08`)
- **Governance**: BAJO
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` §Testing y verificación
  - `knowledge-base/01_vision_y_objetivos.md` §Métricas de éxito
  - `README.md` (creado por C-01)

---

## Plan día a día (2026-11-20 al 2026-11-25)

Hoy es viernes 2026-11-20. La entrega es el miércoles 25. Hay 5 días hábiles de trabajo y un día de colchón.

| Día | Santiago (líder) | Resto del equipo | Hito del día |
|---|---|---|---|
| **Vie 20** | Instalar JDK 17 (PA-01). C-01: explore, propose y apply, PR a `main`. | Instalar JDK 17 y clonar. Leer KB y su ficha. `explore` + `propose` de su change en su rama (sin apply). | PR de C-01 abierto. Propuestas de C-03 a C-07 listas. |
| **Sáb 21** | Mergear C-01 (GATE 0) por la mañana y avisar. `explore` + `propose` de C-02 (cerrar PA-03, PA-04, PA-06, PA-07); empezar apply. | Rebasear sobre `main`. `apply` de su change (C-03 a C-07). | C-01 en `main`. Ola 1 en apply. |
| **Dom 22** | `apply` de C-02 con todos los escenarios; revisar los PRs que lleguen. | Terminar `verify` y abrir PR (C-03, C-04, C-06, C-07). Verificación de fuentes (ver abajo). Valentin prepara la fundación de `design-system`. | PRs de C-03, C-04, C-06, C-07 abiertos o mergeados. C-02 con tests en verde. |
| **Lun 23** | `verify` y `archive` de C-02 (GATE 1). Mergear PRs pendientes. Aprobar fundación `design-system`. | Romero arranca C-08 al mergearse C-02. Castillo cierra C-05. Valentin arranca C-09 (GATE 2) con primer PR del layout. Joaquin prepara `explore` + `propose` de C-10. Adriano: revisión cruzada de PRs y apoyo en tests. | **C-02 archivado.** C-05 mergeado. |
| **Mar 24** | C-11: `./mvnw test` completo, health check, README en máquina limpia. Grabar el video. Consolidar la reflexión. Si C-09/C-10 no llegan, **cortar la UI** y entregar la API. | Joaquin: C-10 (GATE 3) si C-09 y C-08 están en `main`. Resto: guion del video, reflexión individual, últimos arreglos. | Todo mergeado. Video grabado. Reflexión redactada. |
| **Mié 25** | Revisión final y entrega. Colchón para imprevistos. | Colchón. | **Entrega.** |

**Reservas explícitas dentro del plan**
- **Archive del change evaluado**: lunes 23 (tarde). Si C-02 se atrasa, el martes a la mañana es el límite duro, porque archive, verify y el video del ciclo completo dependen de eso.
- **Verificación manual de fuentes** (la consigna exige que una persona abra y compruebe al menos 5 fuentes; ver tabla en `docs/discovery/informe-discovery.md` §Verificación de fuentes): se reparte entre domingo y lunes, una fuente por integrante (fuente 1: Joaquin; 2: Romero; 3: Castillo; 4: Adriano; 5: Valentin), anotando qué encontraron y la corrección aplicada. Santiago consolida el informe el martes.
- **Video**: se graba el martes 24 con C-02 ya archivado y la app corriendo; guion el mismo martes por Joaquin.
- **Reflexión escrita**: aportes individuales martes 24; Santiago consolida el mismo día. El miércoles queda como colchón.
- **Pregunta pendiente con la cátedra (IN-02)**: la consigna admite hasta 3 integrantes y el equipo es de 6. Confirmarla esta semana, antes de la entrega.

---

## Backlog (fuera del roadmap, no son changes planificados)

Ideas registradas para después de la entrega. No tienen número C-NN y no se implementan en este ciclo.

**Próxima etapa**
- US-101: recordatorio automático 24 h antes y confirmación por WhatsApp Business API (se enchufa al puerto `NotificadorDeTurnos`).
- US-102: ficha clínica y odontograma (Ley 26.529).
- US-103: cobros, seña con Mercado Pago y factura electrónica ARCA.

**Posteriores**
- US-110: reprogramar turno (revalidando choques y excluyendo el propio turno).
- US-111: bloqueos y horario laboral por profesional.
- US-112: registro de asistencia y ausentes (`ASISTIO` / `AUSENTE`).
- US-113: reserva del paciente por link.
- US-114: lista de espera para cubrir cancelaciones.
- US-115: obras sociales y prepagas.
- US-116: multisede.

---

## Resumen

| ID | Change | Owner | Ola | Dependencias | Governance | Camino crítico |
|---|---|---|---|---|---|---|
| C-01 | `scaffold-proyecto` | Santiago | 0 | ninguna | CRITICO | Sí |
| C-02 | `crear-turno-sin-solapamientos` | Santiago | 1 | C-01 | ALTO | Sí (evaluado) |
| C-03 | `configurar-recursos` | Joaquin | 1 | C-01 | MEDIO | No |
| C-04 | `gestionar-pacientes` | Romero | 1 | C-01 | MEDIO | No |
| C-05 | `autenticacion-y-roles` | Castillo | 1 | C-01 | CRITICO | No |
| C-06 | `link-whatsapp-turno` | Adriano | 1 | C-01 | BAJO | No |
| C-07 | `agenda-del-dia` | Valentin | 1 | C-01 | MEDIO | No |
| C-08 | `cancelar-turno` | Romero | 2 | C-02 | MEDIO | Sí |
| C-09 | `ui-base-y-gestion` | Valentin | 2 | C-03, C-04, C-05 | MEDIO | No (recortable)* |
| C-10 | `ui-turnos-y-agenda` | Joaquin | 2 | C-09, C-02, C-06, C-07, C-08 | MEDIO | No (recortable)* |
| C-11 | `integracion-y-verificacion-final` | Santiago | 3 | todos los planificados | BAJO | Sí |

**11 changes, 4 fases, 5 gates (GATE 0 a GATE 4), camino crítico de 4 changes.**
**Primer change recomendado**: `C-01 scaffold-proyecto`. Para arrancar, invocá la skill de propuesta instalada con ese nombre exacto.
