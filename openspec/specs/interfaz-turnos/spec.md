# interfaz-turnos Specification

## Purpose
Define las pantallas web con las que la secretaría y el administrador gestionan turnos y el odontólogo consulta su agenda del día, reutilizando las reglas y errores ya publicados por el núcleo del sistema.

## Requirements

### Requirement: Formulario de creación de turno con errores de regla visibles

La interfaz SHALL permitir a ADMIN y SECRETARIA crear un turno desde un formulario con paciente, profesional, sillón, prestación, inicio y marca de sobreturno, y MUST mostrar el `codigo` y el mensaje del error devuelto por la regla sin crear el turno.

#### Scenario: Crear turno desde la vista y verlo en la agenda (feliz)

- **GIVEN** datos ficticios de paciente, profesional, sillón y prestación disponibles
- **WHEN** SECRETARIA envía el formulario con un inicio libre dentro del horario
- **THEN** el turno queda RESERVADO con fin calculado por la duración de la prestación y la vista redirige a la agenda del profesional en esa fecha, donde el turno aparece

#### Scenario: Choque de sillón mostrado en el formulario (error de negocio)

- **GIVEN** un turno RESERVADO que ocupa el sillón elegido con el margen de 5 minutos
- **WHEN** SECRETARIA envía el formulario con ese sillón y profesional
- **THEN** la vista permanece en el formulario, muestra `CHOQUE_SILLON` y su mensaje, y no se guarda ningún turno nuevo

#### Scenario: Sobreturno en otro sillón (borde)

- **GIVEN** el profesional elegido ya tiene un turno a esa hora y el sillón elegido está libre
- **WHEN** SECRETARIA marca la opción de sobreturno y envía el formulario
- **THEN** el turno se crea marcado como sobreturno y aparece en la agenda con esa marca

### Requirement: Detalle y cancelación de turno desde la vista

La vista de detalle SHALL mostrar los datos del turno sin DNI ni teléfono del paciente y MUST permitir a ADMIN y SECRETARIA cancelarlo, reflejando el resultado de la regla de anticipación mínima.

#### Scenario: Cancelar con anticipación suficiente (feliz)

- **GIVEN** un turno RESERVADO con más de 6 horas de anticipación según el reloj del sistema
- **WHEN** SECRETARIA confirma la cancelación desde el detalle
- **THEN** el turno pasa a CANCELADO, deja de aparecer en la agenda y conserva sus datos originales

#### Scenario: Cancelación tardía (borde)

- **GIVEN** un turno RESERVADO con menos de 6 horas de anticipación
- **WHEN** SECRETARIA intenta cancelarlo desde el detalle
- **THEN** la vista muestra `CANCELACION_TARDIA` y el turno sigue RESERVADO

#### Scenario: Segunda cancelación (error de negocio)

- **GIVEN** un turno ya CANCELADO
- **WHEN** SECRETARIA vuelve a intentar cancelarlo
- **THEN** la vista muestra `TURNO_YA_CANCELADO` y no modifica la fecha de cancelación original

### Requirement: Acción de WhatsApp en el detalle del turno

La vista SHALL ofrecer el enlace click-to-chat generado para ADMIN y SECRETARIA y MUST ocultar o deshabilitar la acción cuando el paciente no tenga teléfono, sin mostrar ese caso como error de página.

#### Scenario: Paciente con teléfono (feliz)

- **GIVEN** un turno cuyo paciente ficticio tiene teléfono cargado
- **WHEN** SECRETARIA abre el detalle del turno
- **THEN** la acción de WhatsApp está disponible y apunta a una URL `https://wa.me/` con el mensaje del turno codificado

#### Scenario: Paciente sin teléfono (borde)

- **GIVEN** un turno cuyo paciente no tiene teléfono cargado
- **WHEN** SECRETARIA abre el detalle del turno
- **THEN** la acción de WhatsApp no está disponible y la vista no muestra `PACIENTE_SIN_TELEFONO` como error de pantalla

### Requirement: Agenda del día por rol

La vista de agenda SHALL mostrar los turnos RESERVADOS del profesional y la fecha elegidos, ordenados por inicio, con paciente, prestación, sillón y marca de sobreturno. ADMIN y SECRETARIA SHALL poder elegir profesional; un ODONTOLOGO MUST ver solo la agenda del profesional vinculado a su usuario.

#### Scenario: Agenda con un turno cancelado (feliz)

- **GIVEN** un profesional con tres turnos en la fecha, uno de ellos CANCELADO
- **WHEN** SECRETARIA abre la agenda de ese profesional para esa fecha
- **THEN** la vista muestra los dos turnos activos ordenados por hora, con sus nombres y sin DNI ni teléfono

#### Scenario: Odontólogo no ve la agenda de otro profesional (error de negocio)

- **GIVEN** un ODONTOLOGO vinculado a un profesional
- **WHEN** intenta abrir la agenda de otro profesional
- **THEN** el acceso es denegado con `ACCESO_DENEGADO` y no se muestran turnos ajenos

#### Scenario: Día sin turnos (borde)

- **GIVEN** un profesional sin turnos RESERVADOS en la fecha consultada
- **WHEN** se abre su agenda para esa fecha
- **THEN** la vista muestra un estado vacío claro y no muestra turnos cancelados

### Requirement: Seguridad de las vistas de turnos y agenda

El sistema SHALL exigir sesión y token CSRF en los formularios de las vistas de turnos, y MUST aplicar la matriz de roles: crear, cancelar y ver el link de WhatsApp solo para ADMIN y SECRETARIA; consultar la agenda para los tres roles.

#### Scenario: Formulario sin token CSRF (borde)

- **GIVEN** una sesión autenticada de SECRETARIA
- **WHEN** envía el formulario de creación sin token CSRF válido
- **THEN** la petición es rechazada y no se crea el turno

#### Scenario: Odontólogo accede a acciones de secretaría (error de negocio)

- **GIVEN** un ODONTOLOGO autenticado
- **WHEN** intenta abrir el formulario de creación, cancelar un turno o pedir el link de WhatsApp
- **THEN** el acceso es denegado y no se realiza ninguna acción sobre el turno
