# Design

## Context

C-01 dejó el esqueleto que este change reutiliza sin modificarlo: `TurnoRepository.buscarActivosDeProfesionalEntre(profesionalId, desde, hasta)` (JPQL sobre turnos `RESERVADO`, `inicio >= desde and inicio < hasta`, `order by t.inicio`), el bean `Clock` con la zona del consultorio (`RelojConfig` sobre `TurnosProperties.zona()`), el `Solicitante`/`Rol` de dominio con su puerto `UsuarioActual` (implementación permisiva de C-01, que C-05 reemplaza con `@ConditionalOnMissingBean`), `CodigoError.ACCESO_DENEGADO` (403) y `ManejadorGlobalDeErrores`, que traduce `ReglaDeNegocioException` a `ProblemDetail` con el campo `codigo`. C-02 dejó el patrón de caso de uso por feature con DTO armado dentro de la transacción (`open-in-view: false`). Ver `proposal.md` para la motivación.

## Goals / Non-Goals

**Goals:**

- Exponer `GET /api/agenda?profesionalId=X&fecha=AAAA-MM-DD` con la agenda del día ordenada por hora.
- Resolver el filtro "turnos del día" con el `Clock` inyectado y la zona del consultorio, con bordes deterministas.
- Resolver RN-AG-02 en el caso de uso, con el `Solicitante` como parámetro, testeable sin Spring Security.
- Un test por escenario de la spec, con `Clock.fixed` y fixtures ficticios armados con repositorios de C-01.

**Non-Goals:**

- Conectar el usuario autenticado real: lo hace C-05 reemplazando el bean `UsuarioActual`; este código no cambia (regla anti-colisión 6).
- Pantallas Thymeleaf de la agenda: C-10.
- Validar que el profesional exista o esté activo, o devolver 404 por profesional inexistente: la spec no lo exige y una agenda vacía es una respuesta válida.
- Vista por rango de varios días, filtros por sillón o estado: fuera de US-020.
- Editar archivos compartidos de C-01 ni archivos de C-02/C-03/C-04/C-08 (regla E1).

## Decisions

**D1. Filtro del día: intervalo semiabierto en la zona del consultorio.**

El caso de uso recibe una `LocalDate fecha` y construye `desde = fecha.atStartOfDay()` y `hasta = desde.plusDays(1)`, tomando la zona con `reloj.getZone()` (el bean `Clock` ya está creado con `TurnosProperties.zona()`, es decir `America/Argentina/Buenos_Aires`). Se consulta `buscarActivosDeProfesionalEntre(profesionalId, desde, hasta)`, cuya semántica es `inicio >= desde and inicio < hasta`. Consecuencias exactas:

- 23:59 del día anterior → `inicio < desde` → queda fuera.
- 00:00 del día siguiente → `inicio == hasta` → queda fuera por el límite superior exclusivo.
- 00:00 del día consultado → `inicio == desde` → entra por el límite inferior inclusivo.

`Turno.inicio` ya se persiste como `LocalDateTime` en la zona del consultorio (RN-GL-01), así que **no** se convierte a UTC ni se arma un `ZonedDateTime` con la zona del sistema: se comparan `LocalDateTime` contra los límites calculados. Usar `reloj.getZone()` y no `ZoneId.systemDefault()` es lo que hace que los tests con `Clock.fixed(..., America/Argentina/Buenos_Aires)` sean deterministas (RN-GL-02, D4). *Alternativa*: filtrar en memoria con `fecha.equals(inicio.toLocalDate())` → descartada porque ignora la query existente y trae turnos de más; *alternativa*: `ZonedDateTime`/`Instant` → descartada porque el modelo guarda hora local del consultorio y la conversión introduciría corrimientos en los bordes.

**D2. RN-AG-02 en el caso de uso, con `Solicitante` como parámetro.**

`ConsultarAgenda.ejecutar(Long profesionalId, LocalDate fecha, Solicitante solicitante)` valida primero la visibilidad: si `solicitante.esOdontologo()` y (`solicitante.profesionalId() == null` o no coincide con `profesionalId`), lanza `ReglaDeNegocioException(CodigoError.ACCESO_DENEGADO)`. `SECRETARIA` y `ADMIN` pasan sin restricción. La comprobación va **antes** de consultar la base, así un acceso denegado no lee datos de pacientes. El `Solicitante` llega como parámetro (no se inyecta `UsuarioActual` en el caso de uso) para poder testear RN-AG-02 con JUnit puro, sin contexto de Spring ni `spring-security-test`; el controlador obtiene `usuarioActual.solicitante()` y se lo pasa. El mapeo a 403 lo hace el `ManejadorGlobalDeErrores` de C-01 sin cambios: `ACCESO_DENEGADO` ya tiene estado HTTP 403 y el handler agrega `codigo`. Cuando C-05 reemplace el bean `UsuarioActual`, este código no se toca. *Alternativa*: `@PreAuthorize` o chequeo en el controlador → descartada por J1 (lógica fuera del controlador) y por la regla anti-colisión 6 (los controladores de la ola 1 no llevan anotaciones de seguridad).

**D3. Query reutilizada de C-01: `buscarActivosDeProfesionalEntre`.**

No se agrega query ni se edita `domain/port`. La consulta ya excluye `CANCELADO` (`t.estado = RESERVADO`), filtra por `inicio` en el rango y ordena por `t.inicio`; cubre RN-AG-01 y la exclusión de cancelados (RN-CA-02) sin lógica extra. Si en el futuro hiciera falta otra consulta, se declararía en una interfaz propia dentro de `application/agenda/` (regla anti-colisión 4), no en el puerto de C-01.

**D4. DTO propio armado dentro de la transacción.**

`application/agenda/` define un record `TurnoDeAgendaVista` con: `id`, `inicio`, `fin`, `sobreturno`, `pacienteId`, `pacienteNombre` (nombre y apellido), `prestacionId`, `prestacionNombre`, `sillonId`, `sillonNombre` y `profesionalId`. Se arma dentro del método `@Transactional(readOnly = true)` porque `Turno` tiene relaciones `LAZY` y `open-in-view` está desactivado. No incluye DNI ni teléfono (D1/D2). *Alternativa*: reutilizar `TurnoVista` de C-02 → descartada: solo trae IDs, no los nombres que US-020 necesita mostrar, y editarla tocaría un archivo de C-02.

**D5. Controlador y DTOs de la API en `web/api/agenda/`.**

`AgendaController` (`@RestController`, `@RequestMapping("/api/agenda")`) expone `GET` con `@RequestParam Long profesionalId` y `@RequestParam LocalDate fecha` (Spring convierte ISO `AAAA-MM-DD`; un parámetro faltante o mal formado lo traduce el handler de C-01 a 400 `ENTRADA_INVALIDA`). Sin lógica de negocio (J1): obtiene el `Solicitante` de `UsuarioActual`, delega en `ConsultarAgenda` y mapea a un record de respuesta `AgendaDelDiaResponse(LocalDate fecha, Long profesionalId, List<TurnoDeAgendaResponse> turnos)`. Inyección por constructor (J3). La lista se devuelve en el orden del repositorio (ascendente por `inicio`), sin reordenar en el controlador.

**D6. Ordenamiento estable y desempate.**

El orden observable es ascendente por `inicio` (RN-AG-01). Dos turnos del mismo profesional no pueden compartir `inicio` salvo sobreturno (RN-TU-02/04), y en ese caso el orden entre ellos no es significativo para la spec. No se agrega un segundo criterio de ordenamiento para no depender del `id` generado.

## Risks / Trade-offs

- [El endpoint queda sin enforcement de rol por ruta hasta C-05] → RN-AG-02 se cumple igual en el caso de uso; C-05 debe mapear `GET /api/agenda` para `ADMIN`, `SECRETARIA` y `ODONTOLOGO` (anotarlo en el PR de C-07). Mientras tanto la seguridad de C-01 es permisiva y `UsuarioActual` devuelve `Solicitante.SISTEMA` (ADMIN), lo que permite probar la API.
- [Un profesional inexistente devuelve 200 con lista vacía] → aceptado: la spec no exige 404 y así no se filtra la existencia de profesionales. Queda documentado en Non-Goals.
- [Tests frágiles si se cuela la zona del sistema o `LocalDateTime.now()`] → D4: los límites se calculan con `reloj.getZone()` y los tests usan `Clock.fixed`; ningún test usa la zona por defecto de la JVM.
- [Incluir el nombre del paciente en la respuesta] → es necesario para US-020 y está permitido para usuarios autenticados (RN-DP-02); la spec prohíbe DNI y teléfono y D2 prohíbe loguear datos personales.

## Migration Plan

Sin migración de datos ni cambios de esquema: se apoya en `Turno`, sus índices `(profesional_id, inicio)` y la query existente. La ruta es aditiva; rollback = revert del commit, sin impacto en los changes ya mergeados.

## Open Questions

- Ninguna que afecte la spec, el enfoque o el desglose de tareas.
