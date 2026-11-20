# Spec Delta

## Purpose

Permite dar y consultar turnos de la agenda del consultorio garantizando que ningún profesional ni sillón quede ocupado dos veces a la vez, con margen entre turnos, sobreturno controlado, horario de atención y protección ante reservas simultáneas.

Contexto común de los escenarios (datos ficticios): el consultorio atiende de lunes a viernes de 08:00 a 20:00. El "ahora" del sistema es el lunes 2026-11-23 a las 08:00. Hay dos profesionales, Ana y Bruno, dos sillones, Sillón 1 y Sillón 2, y la prestación Control dura 30 minutos. El margen entre turnos es de 5 minutos. Cuando un escenario dice "existe el turno 09:30-10:00", es un turno RESERVADO de Ana, en el Sillón 1, con Paciente Uno.

## ADDED Requirements

### Requirement: Crear un turno con la duración de la prestación
El sistema SHALL crear un turno con estado RESERVADO cuyo fin es el inicio más la duración de la prestación, ignorando cualquier fin enviado por el cliente, y responder 201 con el turno creado, incluido su fin. El turno MUST registrar quién lo creó y en qué momento.

#### Scenario: Turno libre de choques (feliz)
- **GIVEN** que Ana y el Sillón 1 no tienen turnos el lunes 2026-11-23
- **WHEN** la secretaria crea un turno de Control para Paciente Dos con Ana, en el Sillón 1, a las 10:00
- **THEN** la respuesta es 201 con estado RESERVADO, inicio 10:00 y fin 10:30
- **AND** el turno queda guardado con el usuario que lo creó y la fecha y hora de creación iguales al "ahora"

#### Scenario: El fin enviado por el cliente se ignora
- **WHEN** se crea un turno de Control a las 10:00 enviando además un fin a las 12:00
- **THEN** el turno creado termina a las 10:30

### Requirement: Sillón exclusivo con margen
Un turno nuevo MUST NOT chocar con otro turno activo del mismo sillón, considerando un margen de 5 minutos antes y después. Esto vale también para los sobreturnos. Si choca, el sistema SHALL rechazarlo con 409 y `codigo` igual a `CHOQUE_SILLON`.

#### Scenario: Sillón ocupado (error de negocio)
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Bruno en el Sillón 1 a las 09:45
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_SILLON` y no se guarda ningún turno nuevo

#### Scenario: Empieza 5 minutos después de que termina otro (borde aceptado)
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Bruno en el Sillón 1 a las 10:05
- **THEN** la respuesta es 201

#### Scenario: Empieza justo cuando termina otro (borde rechazado)
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se intenta crear un turno de Bruno en el Sillón 1 a las 10:00
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_SILLON`, porque entre los dos turnos no quedan los 5 minutos de margen

#### Scenario: Termina justo 5 minutos antes de que empiece otro (borde aceptado)
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Bruno en el Sillón 1 a las 08:55 (termina 09:25)
- **THEN** la respuesta es 201

#### Scenario: Un sobreturno no puede compartir el sillón
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un sobreturno de Bruno en el Sillón 1 a las 09:45
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_SILLON`

### Requirement: Profesional sin choques salvo sobreturno
Un turno nuevo MUST NOT chocar con otro turno activo del mismo profesional, considerando un margen de 5 minutos, salvo que se marque como sobreturno. Si choca y no es sobreturno, el sistema SHALL rechazarlo con 409 y `codigo` igual a `CHOQUE_PROFESIONAL`. El sobreturno se exime de este chequeo por completo, margen incluido.

#### Scenario: Profesional ocupado
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Ana en el Sillón 2 a las 09:45
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_PROFESIONAL`

#### Scenario: Sobreturno del mismo profesional en otro sillón
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un sobreturno de Ana en el Sillón 2 a las 09:45
- **THEN** la respuesta es 201 y el turno queda marcado como sobreturno

#### Scenario: El sobreturno no respeta margen con su profesional
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un sobreturno de Ana en el Sillón 2 a las 10:00
- **THEN** la respuesta es 201

### Requirement: Paciente sin turnos superpuestos
Un paciente MUST NOT tener dos turnos activos superpuestos. Para el paciente no hay margen: un turno puede empezar exactamente cuando termina otro. Si se superponen, el sistema SHALL rechazarlo con 409 y `codigo` igual a `CHOQUE_PACIENTE`.

#### Scenario: Paciente con turno superpuesto
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Bruno en el Sillón 2 para Paciente Uno a las 09:45
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_PACIENTE`

#### Scenario: El paciente puede encadenar turnos sin margen
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se crea un turno de Bruno en el Sillón 2 para Paciente Uno a las 10:00
- **THEN** la respuesta es 201

### Requirement: Inicio en múltiplos de 5 minutos
El inicio de un turno MUST tener minutos múltiplos de 5 y segundos en cero. En otro caso, el sistema SHALL rechazarlo con 400 y `codigo` igual a `ENTRADA_INVALIDA`.

#### Scenario: Minuto no múltiplo de 5
- **WHEN** se intenta crear un turno a las 09:03
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA`

#### Scenario: Segundos distintos de cero
- **WHEN** se intenta crear un turno a las 09:05:30
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA`

### Requirement: No crear turnos en el pasado
El inicio de un turno MUST ser posterior al "ahora" del sistema. En otro caso, el sistema SHALL rechazarlo con 422 y `codigo` igual a `TURNO_EN_PASADO`.

#### Scenario: Turno en el pasado
- **WHEN** se intenta crear un turno el viernes 2026-11-20 a las 10:00
- **THEN** la respuesta es 422 con `codigo` igual a `TURNO_EN_PASADO`

#### Scenario: Turno exactamente en el ahora
- **WHEN** se intenta crear un turno el lunes 2026-11-23 a las 08:00
- **THEN** la respuesta es 422 con `codigo` igual a `TURNO_EN_PASADO`

### Requirement: Dentro del horario de atención
El turno completo, desde el inicio hasta el fin, MUST caer dentro de la franja de atención del consultorio para ese día de la semana. En otro caso, incluido un día sin franja o un consultorio sin configurar, el sistema SHALL rechazarlo con 422 y `codigo` igual a `FUERA_DE_HORARIO`.

#### Scenario: Termina después del cierre
- **WHEN** se intenta crear un turno de Control a las 19:45 (terminaría 20:15)
- **THEN** la respuesta es 422 con `codigo` igual a `FUERA_DE_HORARIO`

#### Scenario: Termina justo al cierre
- **WHEN** se crea un turno de Control a las 19:30 (termina 20:00)
- **THEN** la respuesta es 201

#### Scenario: Empieza antes de la apertura
- **WHEN** se intenta crear un turno el martes 2026-11-24 a las 07:45
- **THEN** la respuesta es 422 con `codigo` igual a `FUERA_DE_HORARIO`

#### Scenario: Día sin atención
- **WHEN** se intenta crear un turno el sábado 2026-11-28 a las 10:00
- **THEN** la respuesta es 422 con `codigo` igual a `FUERA_DE_HORARIO`

### Requirement: Recursos existentes y activos
El profesional, el sillón y la prestación MUST existir y estar activos, y el paciente MUST existir. Si falta alguno, el sistema SHALL responder 404 con `codigo` igual a `RECURSO_INEXISTENTE`. Si alguno está inactivo, SHALL responder 422 con `RECURSO_INACTIVO`. Si no se indica sillón, SHALL responder 422 con `SILLON_REQUERIDO`.

#### Scenario: Profesional inexistente
- **WHEN** se intenta crear un turno con un profesional que no existe
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

#### Scenario: Sillón inactivo
- **GIVEN** que el Sillón 2 está desactivado
- **WHEN** se intenta crear un turno en el Sillón 2
- **THEN** la respuesta es 422 con `codigo` igual a `RECURSO_INACTIVO`

#### Scenario: Sin sillón
- **WHEN** se intenta crear un turno sin indicar sillón
- **THEN** la respuesta es 422 con `codigo` igual a `SILLON_REQUERIDO`

### Requirement: Los turnos cancelados no bloquean
Un turno en estado CANCELADO MUST NOT contar para ningún chequeo de choque.

#### Scenario: Horario de un turno cancelado
- **GIVEN** que el turno 09:30-10:00 está cancelado
- **WHEN** se crea un turno de Ana en el Sillón 1 para Paciente Uno a las 09:30
- **THEN** la respuesta es 201

### Requirement: Orden de validación determinista
Cuando un turno viola varias reglas, el sistema SHALL informar solo la primera en este orden: recursos, inicio en múltiplos de 5, pasado, horario, sillón, profesional y paciente.

#### Scenario: Pasado y sillón ocupado a la vez
- **GIVEN** que el "ahora" es el lunes 2026-11-23 a las 11:00 y existe el turno 09:30-10:00
- **WHEN** se intenta crear un turno de Bruno en el Sillón 1 a las 09:45
- **THEN** la respuesta es 422 con `codigo` igual a `TURNO_EN_PASADO`

#### Scenario: Sillón y profesional ocupados a la vez
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se intenta crear un turno de Ana en el Sillón 1 para Paciente Dos a las 09:45
- **THEN** la respuesta es 409 con `codigo` igual a `CHOQUE_SILLON`

### Requirement: Reservas simultáneas
Dos solicitudes concurrentes que crearían turnos en choque sobre el mismo sillón, profesional o paciente MUST NOT dejar ambos turnos guardados. Exactamente una SHALL completarse y la otra SHALL rechazarse con el código de choque que corresponda.

#### Scenario: Dos secretarias reservan el mismo hueco
- **GIVEN** que Ana y el Sillón 1 están libres a las 11:00
- **WHEN** dos solicitudes simultáneas intentan crear un turno de Ana en el Sillón 1 a las 11:00, para pacientes distintos
- **THEN** exactamente una responde 201 y la otra 409
- **AND** queda guardado un solo turno a las 11:00 en el Sillón 1

### Requirement: Consultar un turno
El sistema SHALL devolver el detalle de un turno por su identificador: paciente, profesional, sillón, prestación, inicio, fin, sobreturno, estado y quién lo creó. Si el turno no existe, SHALL responder 404 con `codigo` igual a `RECURSO_INEXISTENTE`. La respuesta MUST NOT incluir DNI ni teléfono del paciente.

#### Scenario: Turno existente
- **GIVEN** que existe el turno 09:30-10:00
- **WHEN** se consulta ese turno por su identificador
- **THEN** la respuesta es 200 con inicio 09:30, fin 10:00, estado RESERVADO y los identificadores de paciente, profesional, sillón y prestación

#### Scenario: Turno inexistente
- **WHEN** se consulta un turno con un identificador que no existe
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`
