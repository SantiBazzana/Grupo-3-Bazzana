# Tasks

## 1. Reglas de dominio (Java puro)

- [x] 1.1 Crear `Intervalo` (record `[inicio, fin)` con `chocaConMargen` y `seSuperpone`) en `domain/rules/turnos`. Verificar con `IntervaloTest`: 10:05 no choca y 10:04 choca después de 09:30-10:00 con margen de 5; superposición simple sin margen; otro intervalo lejano no choca.
- [x] 1.2 Crear `ContextoDeTurno` y `ValidadorDeTurno` con el orden RN-TU-09 (recursos inactivos → RN-TU-11 → RN-TU-06 → RN-TU-07 → RN-TU-03 → RN-TU-02 con exención por sobreturno → RN-TU-05). Verificar con `ValidadorDeTurnoTest`, un test por regla y borde: inicio no múltiplo de 5 y con segundos, pasado y "ahora" exacto, antes de la apertura, termina justo al cierre, termina después del cierre, día sin franja, sillón con margen (también en sobreturno), profesional ocupado, sobreturno sin margen con el profesional, paciente superpuesto y paciente encadenado sin margen, y los dos casos de orden de validación de la spec.

## 2. Caso de uso y bloqueo

- [x] 2.1 Crear el puerto `BloqueoDeRecursos` y `JpaBloqueoDeRecursos` (`PESSIMISTIC_WRITE`, orden sillón → profesional → paciente) en `application/turnos`. Verificar que compila y que `CrearTurno` lo usa en el orden indicado (revisión del test de concurrencia en 3.3).
- [x] 2.2 Implementar `CrearTurno` (D3: `SILLON_REQUERIDO`, bloqueo y carga, `RECURSO_INEXISTENTE`, cálculo del fin, horario del único consultorio, candidatos con margen, validador, guardado con `creadoEn` y `creadoPor`, `TurnoVista`) y `ConsultarTurno`. Verificar con los tests de API del grupo 3.

## 3. API REST y verificación por escenario

- [x] 3.1 Crear `TurnoController` (`POST /api/turnos` → 201 + `Location`; `GET /api/turnos/{id}`), `CrearTurnoRequest` (sin `@NotNull` en `sillonId`) y `TurnoResponse` (sin DNI ni teléfono) en `web/api/turnos`. Verificar con `TurnosApiTest` los escenarios de los requisitos "Crear un turno con la duración de la prestación" (feliz y fin ignorado, incluidos `creadoPor` y `creadoEn`) y "Consultar un turno" (200 sin DNI ni teléfono, y 404).
- [x] 3.2 Completar `TurnosApiTest` con un test por cada escenario restante de la spec: sillón exclusivo (5 escenarios), profesional y sobreturno (3), paciente (2), múltiplos de 5 (2), pasado (2), horario (4), recursos (3), cancelados (1) y orden de validación (2). Verificar que todos pasan con `mvnw test -Dtest=TurnosApiTest`.
- [x] 3.3 Crear `CrearTurnoConcurrenteTest` (contexto y base aislados, sin `@Transactional`, `@DirtiesContext`) con dos hilos sincronizados que reservan el mismo sillón y horario. Verificar exactamente un éxito, un `CHOQUE_SILLON` y un solo turno guardado (escenario "Dos secretarias reservan el mismo hueco"). Correrlo 3 veces seguidas para descartar que sea inestable.

## 4. Integración

- [x] 4.1 Correr la suite completa con `mvnw test` (los 72 tests de C-01 más los de C-02) y verificar que todo está en verde y que `git diff --name-only main` no lista archivos de C-01 (regla E1), salvo la KB actualizada en el explore.

## Workflow follow-up

- Verify: `openspec validate crear-turno-sin-solapamientos --strict`, la suite completa y la revisión de que no haya datos reales ni secretos.
- Archive: `openspec-archive-change crear-turno-sin-solapamientos` (crea `openspec/specs/turnos/spec.md`) y marcar `[x]` en C-02 de `CHANGES.md`.
- PR `feat(turnos): crear turno sin solapamientos (C-02)` a `main` y avisar a Romero que se abrió el GATE 1 (C-08).
