# Design

## Context

La motivación está en `proposal.md`. Lo que condiciona el diseño:

- **C-01 ya provee** (y este change **no edita**):
  - `TurnoRepository`: consultas de choque con ventana `desde`/`hasta` para sillón y profesional, y sin margen para el paciente.
  - `Turno` (valida `fin > inicio` y sillón obligatorio).
  - `HorarioAtencionRepository.findByConsultorioIdAndDiaSemana`, `ConsultorioRepository`, `CodigoError`, `ReglaDeNegocioException`.
  - El bean `Clock`, `TurnosProperties.margen()` y `UsuarioActual`.
- **Paquetes propios de C-02** (anti-colisión de `CHANGES.md`): `domain/rules/turnos`, `application/turnos` y `web/api/turnos`, con sus tests espejo.
- **Reglas duras**: J1 (reglas en Java puro), J2 (DTOs y `ProblemDetail`), D4 (`Clock`), E1 (no tocar archivos compartidos) y E5 (no implementar fuera de la spec).

## Goals / Non-Goals

**Goals:**
- Que cada regla RN-TU se pueda testear en el dominio, sin Spring ni base de datos, y además de punta a punta por la API.
- Que el orden de validación sea determinista (RN-TU-09).
- Que dos reservas concurrentes no dejen un choque persistido (RN-TU-10).

**Non-Goals:**
- Chequeo de roles (C-05), cancelación (C-08), reprogramación (posterior), agenda (C-07) y vistas (C-10).
- Cambiar las consultas, entidades o configuración de C-01.

## Decisions

### D1. Tres capas, todo dentro de los paquetes de C-02

```
web/api/turnos            TurnoController, CrearTurnoRequest, TurnoResponse
        |
application/turnos        CrearTurno, ConsultarTurno (@Transactional)
        |                 BloqueoDeRecursos (puerto) + JpaBloqueoDeRecursos (adaptador)
        v
domain/rules/turnos       Intervalo, ContextoDeTurno, ValidadorDeTurno  (Java puro)
        |
(C-01) domain/port, domain/model, domain/error
```

### D2. Validación pura sobre un contexto ya cargado
El caso de uso carga todo lo que necesita, arma un `ContextoDeTurno` (record) y se lo pasa al `ValidadorDeTurno`. El validador lanza `ReglaDeNegocioException` con la **primera** regla violada, en el orden RN-TU-09: recursos inactivos → RN-TU-11 → RN-TU-06 → RN-TU-07 → RN-TU-03 → RN-TU-02 → RN-TU-05.
- El contexto contiene el "ahora", el inicio y el fin, la marca de sobreturno, la franja del día (opcional), si los recursos están activos, el margen y los **intervalos candidatos** de sillón, profesional y paciente. Esos candidatos salen de las consultas de C-01, que ya aplican la ventana.
- `Intervalo` (record `[inicio, fin)`) tiene `chocaConMargen(otro, margen)` y `seSuperpone(otro)`. El validador vuelve a comprobar cada candidato con `Intervalo` en lugar de confiar solo en que la lista no esté vacía. Así la regla completa queda en Java puro y se testea sin base de datos, incluido el borde abstracto de 4 minutos, que por la API ya no se puede alcanzar (ver D7).
- *Alternativa*: que el validador consulte los repositorios. Se descartó porque mete Spring en el dominio y rompe J1.
- La inexistencia de recursos (`RECURSO_INEXISTENTE`, 404) y el sillón ausente (`SILLON_REQUERIDO`) se detectan **al cargar**, en el caso de uso, antes de armar el contexto. Eso respeta el orden: los recursos van primero.

### D3. Pasos de `CrearTurno` (una transacción)
1. Si `sillonId` es nulo, lanza `SILLON_REQUERIDO`.
2. **Bloquea y carga**, en este orden: sillón, profesional y paciente (D4). Después carga la prestación. Si alguno no existe, lanza `RECURSO_INEXISTENTE`.
3. Calcula `fin = inicio + prestacion.duracion()` (RN-TU-01).
4. Busca el horario: toma el único consultorio y su franja para `inicio.getDayOfWeek()`. Si no hay consultorio o franja, la franja queda vacía y el validador responde `FUERA_DE_HORARIO`.
5. Consulta los candidatos, con margen `M` de `TurnosProperties`:
   - sillón y profesional entre `inicio - M` y `fin + M`;
   - paciente entre `inicio` y `fin`.
6. Arma el `ContextoDeTurno` con `LocalDateTime.now(clock)` y llama a `ValidadorDeTurno.validar(...)`.
7. Guarda un `new Turno(..., sobreturno, ahora, usuarioActual.solicitante().usuario())` y devuelve una `TurnoVista` (record con ids y datos), armada **dentro** de la transacción porque `open-in-view` está desactivado.

### D4. Bloqueo pesimista con un adaptador propio (cierra PA-06)
- El puerto `BloqueoDeRecursos` (en `application/turnos`) define `Optional<Sillon> bloquearSillon(id)`, `Optional<Profesional> bloquearProfesional(id)` y `Optional<Paciente> bloquearPaciente(id)`.
- Lo implementa `JpaBloqueoDeRecursos` (en el mismo paquete) con `EntityManager.find(Clase, id, LockModeType.PESSIMISTIC_WRITE)`, que genera `SELECT ... FOR UPDATE`.
- **Orden fijo** sillón → profesional → paciente, para que dos transacciones no se bloqueen mutuamente (deadlock). Con la segunda transacción esperando a que la primera haga commit, la consulta de choques posterior ya ve el turno recién guardado (`READ_COMMITTED`).
- *Desviación aceptada*: hay JPA en `application/turnos`. Se acepta para no tocar los puertos compartidos de C-01 (regla E1) en el change que se evalúa.
- *Alternativas*:
  - Un PR `chore(shared)` que agregue métodos de bloqueo a los puertos de C-01: se posterga.
  - Aislamiento `SERIALIZABLE`: se descartó porque en H2 no protege bien las consultas por rango.

### D5. API y DTOs
- `POST /api/turnos`: el cuerpo es `CrearTurnoRequest(pacienteId, profesionalId, sillonId, prestacionId, inicio, sobreturno)`.
  - `@NotNull` en paciente, profesional, prestación e inicio. `sillonId` **no** lleva `@NotNull`, para que su ausencia responda `SILLON_REQUERIDO` 422 (spec) y no 400.
  - `sobreturno` es opcional (`false` por defecto).
  - Un campo `fin` en el JSON se ignora: el record no lo tiene y Spring Boot no falla ante propiedades desconocidas.
- Respuesta: `201 Created`, `Location: /api/turnos/{id}` y `TurnoResponse(id, pacienteId, profesionalId, sillonId, prestacionId, inicio, fin, sobreturno, estado, creadoPor)`. **Sin** DNI ni teléfono (D1/D2 de `AGENTS.md`).
- `GET /api/turnos/{id}`: `ConsultarTurno` (transacción de solo lectura) devuelve `200` con `TurnoResponse`, o `RECURSO_INEXISTENTE` 404.
- El controlador no tiene lógica: traduce el DTO y delega en el caso de uso (J1).

### D6. Tests
- **Dominio**: `IntervaloTest` y `ValidadorDeTurnoTest` con JUnit y AssertJ, sin Spring. Cubren cada regla, los bordes, el sobreturno y el orden de validación.
- **API**: `TurnosApiTest` (`@SpringBootTest` + MockMvc + `@Transactional`, que hace rollback en cada test).
  - Un test por escenario de la spec.
  - El reloj se fija con `@TestBean(name = "reloj")` en lunes 2026-11-23 08:00.
  - Los datos ficticios se arman con `DatosDePrueba` de C-01.
- **Concurrencia**: `CrearTurnoConcurrenteTest` (`@SpringBootTest` **sin** `@Transactional`, con una propiedad propia para tener un contexto y una base aislados, y `@DirtiesContext`).
  - Dos hilos sincronizados con `CountDownLatch` llaman a `CrearTurno` sobre el mismo sillón y horario.
  - Se espera exactamente un éxito, un `CHOQUE_SILLON` y un solo turno guardado.

### D7. RN-TU-11 y el borde del margen
- RN-TU-11 se cumple con `minuto % 5 == 0`, `segundo == 0` y `nano == 0`.
- Como los inicios y las duraciones son múltiplos de 5, por la API los huecos entre turnos también lo son. Por eso el borde observable del margen es **10:00 (choca) contra 10:05 (no choca)**, después de un turno que termina a las 10:00.
- El borde de "4 minutos" de la KB sigue siendo cierto como definición, y se prueba en `IntervaloTest`.

### D8. Un único consultorio
Se toma el primero que devuelve `ConsultorioRepository.findAll()`: en la v1 hay uno solo (KB 04). Si hubiera multisede (posterior), este paso tendría que recibir el consultorio.

## Risks / Trade-offs

- **[Timeout de bloqueo en H2 si una transacción tarda]** → Las transacciones son cortas (pocas consultas). Si apareciera `LockTimeout`, el manejador global lo devuelve como `ERROR_INTERNO`; queda registrado para revisar al pasar a PostgreSQL.
- **[JPA en la capa application (D4)]** → Está encapsulado detrás de `BloqueoDeRecursos`. Mover el adaptador a `infrastructure/` es un cambio mecánico para después.
- **[Todas las reservas de un mismo profesional se serializan]** → Es aceptable para un consultorio de 1 a 5 profesionales.
- **[El borde de 4 minutos de la KB ya no se alcanza por la API]** → Se documenta en D7. El escenario equivalente de la spec usa 10:00 contra 10:05.
- **[Contextos de test que comparten datos]** → Los tests de la API hacen rollback. El test concurrente usa un contexto propio y `@DirtiesContext`.
