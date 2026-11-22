# agenda Specification

## Purpose
Expone la agenda diaria de un profesional: los turnos activos de una fecha, en la zona del consultorio, ordenados por hora y con los datos necesarios para organizar la jornada, respetando que un odontólogo solo vea su propia agenda.

## Requirements

### Requirement: Agenda diaria de un profesional

El sistema SHALL devolver, para un profesional y una fecha, los turnos en estado RESERVADO cuyo `inicio` cae en esa fecha en la zona del consultorio, ordenados ascendentemente por `inicio`. Cada turno MUST incluir identificador, `inicio`, `fin`, la marca de sobreturno, el nombre e identificador del paciente, el nombre e identificador de la prestación y el nombre e identificador del sillón. Los turnos CANCELADO MUST quedar excluidos (RN-AG-01, RN-CA-02).

#### Scenario: Tres turnos del día, uno cancelado (feliz)

- **GIVEN** que Ana tiene el lunes 2026-11-23 tres turnos: 09:00, 10:00 (cancelado) y 11:30
- **WHEN** se consulta la agenda de Ana para el 2026-11-23
- **THEN** la respuesta es 200 con 2 turnos, en el orden 09:00 y 11:30
- **AND** cada turno trae el nombre del paciente, de la prestación y del sillón, y su marca de sobreturno

#### Scenario: Día sin turnos (borde)

- **GIVEN** que Ana no tiene turnos el martes 2026-11-24
- **WHEN** se consulta la agenda de Ana para el 2026-11-24
- **THEN** la respuesta es 200 con una lista vacía

#### Scenario: Sobreturno marcado (borde)

- **GIVEN** que Ana tiene un turno marcado como sobreturno el lunes 2026-11-23 a las 12:00
- **WHEN** se consulta la agenda de Ana para el 2026-11-23
- **THEN** la respuesta es 200 y ese turno aparece con la marca de sobreturno en verdadero

### Requirement: Límites del día en la zona del consultorio

La fecha consultada MUST interpretarse como el intervalo semiabierto `[00:00:00, 00:00:00 del día siguiente)` en `America/Argentina/Buenos_Aires` (RN-GL-01). Un turno con `inicio` anterior al límite inferior o igual al límite superior MUST quedar fuera de la agenda. La zona MUST obtenerse del `Clock` inyectado y nunca de la zona del sistema (RN-GL-02).

#### Scenario: Turno a las 23:59 del día anterior (borde)

- **GIVEN** que Ana tiene un turno el domingo 2026-11-22 a las 23:59
- **WHEN** se consulta la agenda de Ana para el 2026-11-23
- **THEN** la respuesta es 200 y ese turno no aparece

#### Scenario: Turno a las 00:00 del día siguiente (borde)

- **GIVEN** que Ana tiene un turno el martes 2026-11-24 a las 00:00
- **WHEN** se consulta la agenda de Ana para el 2026-11-23
- **THEN** la respuesta es 200 y ese turno no aparece

#### Scenario: Turno a las 00:00 del día consultado (borde inclusivo)

- **GIVEN** que Ana tiene un turno el lunes 2026-11-23 a las 00:00
- **WHEN** se consulta la agenda de Ana para el 2026-11-23
- **THEN** la respuesta es 200 y ese turno aparece en la agenda

### Requirement: Visibilidad de la agenda según el rol

El sistema SHALL permitir que un usuario con rol ODONTOLOGO consulte solo la agenda del profesional vinculado a su usuario. Los roles SECRETARIA y ADMIN SHALL poder consultar la agenda de cualquier profesional. Si un ODONTOLOGO pide la agenda de otro profesional o no tiene un profesional vinculado, el sistema MUST responder 403 con `codigo` igual a `ACCESO_DENEGADO` (RN-AG-02).

#### Scenario: Un odontólogo pide la agenda de otro (error de negocio)

- **GIVEN** que Ana es ODONTOLOGO vinculada al profesional Ana y pide la agenda del profesional Bruno
- **WHEN** se consulta `GET /api/agenda?profesionalId=<Bruno>&fecha=2026-11-23`
- **THEN** la respuesta es 403 con `codigo` igual a `ACCESO_DENEGADO`

#### Scenario: Un odontólogo pide su propia agenda

- **GIVEN** que Ana es ODONTOLOGO vinculada al profesional Ana
- **WHEN** se consulta `GET /api/agenda?profesionalId=<Ana>&fecha=2026-11-23`
- **THEN** la respuesta es 200 con la agenda de Ana

#### Scenario: La secretaria pide la agenda de cualquier profesional

- **GIVEN** que el solicitante es SECRETARIA sin profesional vinculado
- **WHEN** se consulta la agenda del profesional Ana para el 2026-11-23
- **THEN** la respuesta es 200 con la agenda de Ana

#### Scenario: El administrador pide la agenda de cualquier profesional

- **GIVEN** que el solicitante es ADMIN
- **WHEN** se consulta la agenda del profesional Bruno para el 2026-11-23
- **THEN** la respuesta es 200 con la agenda de Bruno

#### Scenario: Un odontólogo sin profesional vinculado (borde)

- **GIVEN** que el solicitante es ODONTOLOGO y no tiene profesional vinculado
- **WHEN** se consulta la agenda de cualquier profesional
- **THEN** la respuesta es 403 con `codigo` igual a `ACCESO_DENEGADO`

### Requirement: Sin datos personales sensibles en la respuesta

La respuesta de la agenda MUST NOT incluir el DNI ni el teléfono del paciente. La consulta MUST NOT registrar en logs el DNI, el teléfono ni el nombre completo del paciente (RN-DP-03, D2).

#### Scenario: La respuesta no expone DNI ni teléfono

- **GIVEN** que la agenda de Ana incluye un turno del Paciente Uno
- **WHEN** se consulta la agenda de Ana
- **THEN** la respuesta es 200 y ninguno de sus turnos incluye DNI ni teléfono del paciente
