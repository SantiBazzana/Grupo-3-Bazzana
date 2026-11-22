# Proposal

## Why

Hoy un turno `RESERVADO` solo se puede consultar por su identificador: no existe una vista de la jornada. El odontólogo necesita ver de un vistazo sus turnos del día, ordenados por hora y con el paciente, la prestación y el sillón, para organizar su trabajo (US-020, flujo 3). Sin esta lectura, la agenda que el producto promete no existe y C-10 no tiene API que consumir. Además, como la agenda expone datos de pacientes, el odontólogo no puede ver la de otro profesional (RN-AG-02).

## What Changes

- Nuevo caso de uso `ConsultarAgenda(profesionalId, fecha, solicitante)` en `application/agenda/`: devuelve los turnos `RESERVADO` del profesional cuyo `inicio` cae en la fecha pedida, en la zona del consultorio, ordenados por `inicio`, con paciente, prestación, sillón, horario y marca de sobreturno (RN-AG-01).
- Filtro de "turnos del día" con el `Clock` inyectado y la zona `America/Argentina/Buenos_Aires`: el día es el intervalo semiabierto `[00:00, 00:00 del día siguiente)`, así que un turno a las 23:59 del día anterior y uno a las 00:00 del día siguiente quedan fuera.
- RN-AG-02 resuelta dentro del caso de uso con el `Solicitante` recibido como parámetro: un `ODONTOLOGO` solo ve su propia agenda; `SECRETARIA` y `ADMIN` ven la de cualquiera. Si un odontólogo pide la agenda de otro, se lanza una excepción de dominio que el manejador global traduce a **403** con `codigo = ACCESO_DENEGADO`.
- Nuevo endpoint `GET /api/agenda?profesionalId=X&fecha=AAAA-MM-DD` en `web/api/agenda/`, con DTOs propios; reutiliza la consulta de turnos activos por profesional y rango de fechas que C-01 ya declara en `TurnoRepository`.
- No cambia la creación, la consulta por id ni la cancelación de turnos. No se edita ningún archivo compartido de C-01 ni de C-02/C-03/C-04/C-08.

## Capabilities

### New Capabilities

- `agenda`: consulta de la agenda diaria de un profesional — turnos activos de una fecha en la zona del consultorio, ordenados por hora, con sus datos de presentación y la regla de visibilidad por rol (RN-AG-01, RN-AG-02).

### Modified Capabilities

_(ninguna)_

## Impact

- **Código nuevo** (scope de C-07 en `CHANGES.md`): `application/agenda/**`, `web/api/agenda/**` y tests espejo en `src/test/java/ar/odonto/turnos/application/agenda/` y `src/test/java/ar/odonto/turnos/web/api/agenda/`.
- **Código existente reutilizado sin modificar**: `TurnoRepository.buscarActivosDeProfesionalEntre` (ya filtra `RESERVADO`, ordena por `inicio` y usa `inicio >= desde`, `inicio < hasta`), `Solicitante`/`Rol`, `UsuarioActual` (implementación permisiva de C-01, que C-05 reemplaza sin tocar este código), `CodigoError.ACCESO_DENEGADO`, `ReglaDeNegocioException`, `ManejadorGlobalDeErrores` y el bean `Clock` de `RelojConfig`.
- **Archivos compartidos**: no se editan `pom.xml`, `domain/model/**`, `domain/port/**`, `domain/error/**`, `infrastructure/**`, `web/api/error/**`, `application*.yml` ni `README.md` (regla E1). Si faltara una query, se declara una interfaz propia en `application/agenda/`; no se toca el repositorio de C-01.
- **API**: nueva ruta `GET /api/agenda` (200 con la agenda; 403 con Problem Detail y `codigo = ACCESO_DENEGADO` cuando un odontólogo pide la agenda de otro; 400 por parámetros faltantes o mal formados, traducido por el manejador global).
- **Dependencias**: solo C-01 (archivado y mergeado). C-05 conectará el `Solicitante` real sin cambiar este código; C-10 consumirá esta API.
- **Datos**: fixtures 100 % ficticios (D1); la respuesta no incluye DNI ni teléfono del paciente y los logs no registran datos personales (D2/RN-DP-03).
