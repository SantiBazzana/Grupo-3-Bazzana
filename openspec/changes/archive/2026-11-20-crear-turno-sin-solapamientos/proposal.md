# Proposal

## Why

La secretaria de un consultorio con varios odontólogos y sillones necesita dar turnos sin generar dobles reservas: el mismo profesional o el mismo sillón ocupados a la vez. Es el problema central que identificó el Discovery, y ninguno de los 19 competidores relevados documenta la validación **conjunta** de profesional y sillón. C-01 dejó el modelo, las consultas de choque y el manejo de errores; falta la operación de negocio que los usa.

**Por qué elegimos este change para el ciclo OPSX evaluado:**
- Pertenece al MVP imprescindible que recomendamos en la sección D del informe de Discovery.
- Tiene reglas de negocio verificables: margen de 5 minutos, sillón exclusivo, sobreturno y horario.
- Es una sola funcionalidad (crear un turno), lo bastante chica para terminarla, testearla y archivarla antes del 2026-11-25.

## What Changes

- Nueva operación **crear turno** (`POST /api/turnos`): recibe paciente, profesional, sillón, prestación, hora de inicio y la marca opcional de sobreturno. Calcula el fin con la duración de la prestación y aplica las reglas RN-TU-01 a RN-TU-11 en un orden fijo. Si las pasa, persiste el turno como `RESERVADO`. Si no, rechaza con el `codigo` de la primera regla violada.
- Nueva consulta **detalle de turno** (`GET /api/turnos/{id}`).
- **Regla nueva RN-TU-11**: el inicio va en múltiplos de 5 minutos. Ya está registrada en `knowledge-base/05_reglas_de_negocio.md`.
- **Protección ante reservas simultáneas** (RN-TU-10): dos creaciones concurrentes sobre el mismo recurso no pueden dejar un choque persistido.
- Fuera de alcance: cancelar (C-08), reprogramar (posterior), agenda (C-07), permisos por rol (C-05) y pantallas (C-10).

## Resumen del explore (2026-11-20)

- **Código existente (C-01)**: ya están las consultas de choque con margen por sillón y profesional, y sin margen por paciente, todas probadas en los bordes 10:04 y 10:05. También existen `Turno`, `HorarioAtencion` por consultorio y día, `CodigoError`, el `Clock` en zona AR y `UsuarioActual`. No hay una consulta "bloqueante" ni una que devuelva "el consultorio actual".
- **Decisiones del líder**:
  - PA-03: el sobreturno saltea todo el chequeo del profesional, margen incluido.
  - PA-04: este change no chequea el rol; lo hace C-05 por ruta.
  - PA-07: el inicio va en múltiplos de 5 minutos (nueva RN-TU-11).
  - PA-06: bloqueo pesimista de sillón, profesional y paciente, con un adaptador propio de este change.
  - Un día sin franja de atención, o sin consultorio cargado, se rechaza con `FUERA_DE_HORARIO`.
- **KB actualizada antes de proponer** (regla D3): RN-TU-09, RN-TU-10 y RN-TU-11 en `05`, SU-04 y SU-05 en `09`, y las preguntas resueltas en `10`.

## Capabilities

### New Capabilities
- `turnos`: creación y consulta de turnos de la agenda, con las reglas de duración, choques por sillón, profesional y paciente, sobreturno, horario de atención, inicio en múltiplos de 5 minutos y protección ante reservas simultáneas.

### Modified Capabilities
<!-- Ninguna: base-del-sistema no cambia; esta capacidad usa su formato de errores y su reloj. -->

## Impact

- **Código nuevo, solo en los paquetes de C-02** (reglas anti-colisión de `CHANGES.md`): `domain/rules/turnos/`, `application/turnos/` y `web/api/turnos/`, con los tests espejo. **No** se edita ningún archivo de C-01.
- **API**: `POST /api/turnos` (201 / 400 / 404 / 409 / 422) y `GET /api/turnos/{id}` (200 / 404), con errores *Problem Details* y `codigo`.
- **Dependencias**: ninguna nueva.
- **Equipo**: desbloquea C-08 `cancelar-turno` (Romero), que usa la misma capacidad `turnos` y prueba que cancelar libera el horario.
- **Governance**: ALTO. Son las reglas núcleo del producto y es el change evaluado.
