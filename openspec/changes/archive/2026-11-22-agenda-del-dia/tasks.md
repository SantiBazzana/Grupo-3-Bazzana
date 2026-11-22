# Tasks

## 1. Caso de uso ConsultarAgenda

- [x] 1.1 Crear `application/agenda/TurnoDeAgendaVista` (record con id, inicio, fin, sobreturno, pacienteId, pacienteNombre, prestacionId, prestacionNombre, sillonId, sillonNombre, profesionalId, sin DNI ni teléfono) y `application/agenda/ConsultarAgenda` (`@Service`, `@Transactional(readOnly = true)`, inyección por constructor con `TurnoRepository` y `Clock`), que arma el DTO dentro de la transacción; verificar con un test de aplicación que una agenda con un turno devuelve sus nombres e IDs y no incluye DNI ni teléfono.
- [x] 1.2 Implementar el filtro del día con `desde = fecha.atStartOfDay()` y `hasta = desde.plusDays(1)` usando `reloj.getZone()`, delegando en `TurnoRepository.buscarActivosDeProfesionalEntre`; verificar con tests de aplicación y `Clock.fixed` que el turno de las 23:59 del día anterior y el de las 00:00 del día siguiente quedan fuera, que el de las 00:00 del día consultado entra y que un día sin turnos devuelve lista vacía.
- [x] 1.3 Verificar con tests de aplicación el escenario feliz (3 turnos, uno CANCELADO → devuelve 2 ordenados por `inicio`), la exclusión de CANCELADO y que un sobreturno se devuelve con `sobreturno = true`; usar fixtures ficticios armados con los repositorios de C-01, sin depender de C-02.
- [x] 1.4 Implementar RN-AG-02 antes de consultar la base: si el `Solicitante` es `ODONTOLOGO` y su `profesionalId` es nulo o distinto del pedido, lanzar `ReglaDeNegocioException(CodigoError.ACCESO_DENEGADO)`; verificar con tests de aplicación que el odontólogo con agenda ajena y el odontólogo sin profesional vinculado son denegados, y que el odontólogo con su propia agenda, la secretaria y el admin son aceptados.

## 2. Endpoint GET /api/agenda

- [x] 2.1 Crear `web/api/agenda/AgendaController` (`GET /api/agenda` con `profesionalId` y `fecha` ISO) y los records de respuesta (`AgendaDelDiaResponse` y `TurnoDeAgendaResponse`), sin lógica de negocio, obteniendo el `Solicitante` de `UsuarioActual`; verificar con `@SpringBootTest` + MockMvc que la consulta feliz responde 200 con los 2 turnos ordenados y sus campos de presentación.
- [x] 2.2 Verificar con MockMvc el error de negocio: un odontólogo pidiendo la agenda de otro recibe 403 con `codigo = ACCESO_DENEGADO`, y que la respuesta no incluye DNI ni teléfono del paciente.
- [x] 2.3 Verificar con MockMvc que la ausencia o el formato inválido de `profesionalId`/`fecha` responde 400 con `codigo = ENTRADA_INVALIDA`, traducido por el manejador global de C-01 (sin agregar manejo propio).

## 3. Verificación final

- [x] 3.1 Correr `mvnw.cmd test` completo en verde, sin tests `@Disabled` ni `-DskipTests` (regla J4), con al menos un test por escenario de `specs/agenda/spec.md`; verificar también `openspec validate agenda-del-dia --strict` sin errores.
- [x] 3.2 Confirmar con `git diff --stat` contra `main` que solo se agregó código en `application/agenda/**`, `web/api/agenda/**` y los tests espejo, sin tocar archivos compartidos de C-01 ni de otros changes (regla E1).

## Workflow follow-up

- Abrir PR de `feat/agenda-del-dia` a `main` (nunca commitear directo a `main`, E2) con conventional commits en español (E4).
- Avisar a C-05 que debe mapear `GET /api/agenda` para ADMIN, SECRETARIA y ODONTOLOGO en su cadena de seguridad.
- Archivar el change con `openspec archive agenda-del-dia` cuando el PR esté mergeado y marcar `[x]` en `CHANGES.md` §C-07.
