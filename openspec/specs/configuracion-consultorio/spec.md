# configuracion-consultorio Specification

## Purpose

Permite configurar el consultorio desde la API: dar de alta, editar, listar y desactivar profesionales, sillones y prestaciones, y consultar o reemplazar la franja de atención de cada día de la semana, reutilizando las invariantes RN-CF-01 a RN-CF-04 ya presentes en el modelo.

## Requirements

### Requirement: Alta de prestación con duración válida

El sistema SHALL permitir crear una prestación con nombre único y `duracionMinutos` entero mayor que 0 y múltiplo de 5, respondiendo 201 con su representación y la ubicación del recurso. El nombre de la prestación MUST ser único (RN-CF-01, RN-CF-04).

#### Scenario: Alta feliz de Control

- **WHEN** se envía `POST /api/prestaciones` con `{"nombre":"Control","duracionMinutos":30}`
- **THEN** la respuesta es 201 con `nombre` "Control", `duracionMinutos` 30 y `activa` true, e incluye la ubicación del recurso creado

#### Scenario: Duración no múltiplo de 5

- **WHEN** se envía `POST /api/prestaciones` con `duracionMinutos` 7
- **THEN** la respuesta es 422 con `codigo` igual a `DURACION_INVALIDA` y no se guarda la prestación

#### Scenario: Duración cero

- **WHEN** se envía `POST /api/prestaciones` con `duracionMinutos` 0
- **THEN** la respuesta es 422 con `codigo` igual a `DURACION_INVALIDA`

### Requirement: Edición de prestaciones

El sistema SHALL permitir editar el nombre, la duración y el estado activo de una prestación existente mediante `PUT /api/prestaciones/{id}`, respondiendo 200 con la representación actualizada. La duración enviada MUST cumplir RN-CF-01 y el nombre MUST ser único entre las prestaciones.

#### Scenario: Editar duración y desactivar

- **GIVEN** existe la prestación "Control" de 30 minutos
- **WHEN** se envía `PUT /api/prestaciones/{id}` con `duracionMinutos` 45 y `activa` false
- **THEN** la respuesta es 200 con `duracionMinutos` 45 y `activa` false

#### Scenario: Editar con duración inválida

- **WHEN** se envía `PUT /api/prestaciones/{id}` con `duracionMinutos` 7
- **THEN** la respuesta es 422 con `codigo` igual a `DURACION_INVALIDA` y la prestación conserva su duración anterior

#### Scenario: Prestación inexistente

- **WHEN** se envía `PUT /api/prestaciones/{id}` con un id que no existe
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

### Requirement: Alta, edición y listado de sillones

El sistema SHALL permitir crear, editar y listar sillones con nombre único (RN-CF-04), exponiendo `GET` y `POST /api/sillones` y `PUT /api/sillones/{id}`. El listado MUST incluir tanto sillones activos como inactivos. Un nombre duplicado SHALL rechazarse con 409 y `codigo` igual a `DATO_DUPLICADO`.

#### Scenario: Alta de sillón

- **WHEN** se envía `POST /api/sillones` con `{"nombre":"Sillon 1"}`
- **THEN** la respuesta es 201 con `nombre` "Sillon 1" y `activo` true

#### Scenario: Nombre de sillón duplicado

- **GIVEN** existe el sillón "Sillon 1"
- **WHEN** se envía `POST /api/sillones` con el mismo nombre
- **THEN** la respuesta es 409 con `codigo` igual a `DATO_DUPLICADO`

#### Scenario: Editar sin cambiar el nombre

- **GIVEN** existe el sillón "Sillon 1"
- **WHEN** se envía `PUT /api/sillones/{id}` con el mismo nombre y `activo` false
- **THEN** la respuesta es 200 y no se devuelve `DATO_DUPLICADO`, porque el nombre no cambió

#### Scenario: Sillón inexistente

- **WHEN** se envía `PUT /api/sillones/{id}` con un id que no existe
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

### Requirement: Alta, edición y listado de profesionales

El sistema SHALL permitir crear, editar y listar profesionales con matrícula única (RN-CF-04), exponiendo `GET` y `POST /api/profesionales` y `PUT /api/profesionales/{id}`. La matrícula MUST ser inmutable en la edición; si se envía distinta de la actual, la solicitud SHALL rechazarse con 400 y `codigo` igual a `ENTRADA_INVALIDA`.

#### Scenario: Alta de profesional

- **WHEN** se envía `POST /api/profesionales` con nombre "Ana", apellido "Ejemplo", matrícula "M-0001" y teléfono ficticio
- **THEN** la respuesta es 201 con la matrícula "M-0001" y `activo` true

#### Scenario: Matrícula duplicada

- **GIVEN** existe el profesional con matrícula "M-0001"
- **WHEN** se envía `POST /api/profesionales` con la misma matrícula
- **THEN** la respuesta es 409 con `codigo` igual a `DATO_DUPLICADO`

#### Scenario: Editar sin cambiar la matrícula

- **GIVEN** existe el profesional con matrícula "M-0001"
- **WHEN** se envía `PUT /api/profesionales/{id}` con la misma matrícula y `activo` false
- **THEN** la respuesta es 200 y no se devuelve `DATO_DUPLICADO`

#### Scenario: Matrícula enviada distinta en la edición

- **WHEN** se envía `PUT /api/profesionales/{id}` con una matrícula distinta de la actual
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA` y la matrícula no se modifica

#### Scenario: Profesional inexistente

- **WHEN** se envía `PUT /api/profesionales/{id}` con un id que no existe
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

### Requirement: Alta de profesional sin vínculo de usuario

La administración de profesionales SHALL dejar el registro vinculable en el modelo, pero MUST NOT crear usuarios ni vínculos con usuarios `ODONTOLOGO`: esa vinculación efectiva pertenece al change de autenticación (C-05). La representación del profesional MUST NOT exponer credenciales ni datos de usuario.

#### Scenario: Alta sin usuario asociado

- **WHEN** se crea un profesional con matrícula "M-0002"
- **THEN** la respuesta contiene solo los datos del profesional y no incluye usuario, contraseña ni identificador de usuario vinculado

### Requirement: Baja lógica sin borrado físico

El sistema SHALL desactivar profesionales, sillones y prestaciones mediante `PUT` con `activo`/`activa` false, sin eliminarlos físicamente (RN-CF-03). Un recurso desactivado MUST conservar sus turnos existentes y SHALL poder reactivarse con el mismo `PUT`. No se exponen operaciones `DELETE` para estos recursos.

#### Scenario: Desactivar sillón con turnos

- **GIVEN** el sillón "Sillon 1" tiene un turno RESERVADO
- **WHEN** se envía `PUT /api/sillones/{id}` con `activo` false
- **THEN** la respuesta es 200 con `activo` false y el turno existente sigue consultable con ese mismo sillón

#### Scenario: Reactivar sillón

- **GIVEN** el sillón "Sillon 1" está desactivado
- **WHEN** se envía `PUT /api/sillones/{id}` con `activo` true
- **THEN** la respuesta es 200 con `activo` true

#### Scenario: Sin borrado físico

- **GIVEN** existe el sillón "Sillon 1"
- **WHEN** se envía `DELETE /api/sillones/{id}`
- **THEN** la operación no se ejecuta y el sillón sigue existiendo y listándose

### Requirement: Listado de recursos configurables

El sistema SHALL devolver con `GET /api/profesionales`, `GET /api/sillones` y `GET /api/prestaciones` la totalidad de los recursos configurados, activos e inactivos, en una representación de datos sin entidades de persistencia. Una base sin configuración SHALL devolver una lista vacía.

#### Scenario: Listado con recursos mixtos

- **GIVEN** existe un sillón activo y un sillón desactivado
- **WHEN** se consulta `GET /api/sillones`
- **THEN** la respuesta es 200 y contiene ambos sillones con su estado `activo`

#### Scenario: Sin recursos configurados

- **WHEN** se consulta `GET /api/prestaciones` sobre una base vacía
- **THEN** la respuesta es 200 con una lista vacía

### Requirement: Lectura del horario de atención

El sistema SHALL devolver con `GET /api/consultorio/horario` una entrada por cada día de la semana, con `apertura` y `cierre` en formato `HH:mm`, o ambos `null` cuando el día está cerrado. Debe existir como máximo una franja por consultorio y día (RN-CF-02); un día sin franja SHALL considerarse cerrado (SU-03 / PA-02).

#### Scenario: Consultorio sin franjas

- **WHEN** se consulta `GET /api/consultorio/horario` sin franjas configuradas
- **THEN** la respuesta es 200 con siete días y `apertura` y `cierre` iguales a `null` en todos ellos

#### Scenario: Día con franja configurada

- **GIVEN** el lunes tiene la franja 08:00-20:00
- **WHEN** se consulta `GET /api/consultorio/horario`
- **THEN** la entrada del lunes tiene `apertura` "08:00" y `cierre` "20:00" y los días sin franja siguen en `null`

### Requirement: Reemplazo de la franja de un día

El sistema SHALL reemplazar con `PUT /api/consultorio/horario` la franja del `diaSemana` indicado, respondiendo 200 con la franja resultante. La `apertura` MUST ser anterior al `cierre` (RN-CF-02); en caso contrario, SHALL responder 422 con `codigo` igual a `HORARIO_INVALIDO`. Un día SHALL tener como máximo una franja después de la operación.

#### Scenario: Configurar una franja nueva

- **WHEN** se envía `PUT /api/consultorio/horario` con `diaSemana` "MONDAY", `apertura` "08:00" y `cierre` "20:00"
- **THEN** la respuesta es 200 con esa franja y una consulta posterior devuelve 08:00-20:00 para el lunes

#### Scenario: Reemplazar franja existente

- **GIVEN** el lunes tiene la franja 08:00-20:00
- **WHEN** se envía `PUT /api/consultorio/horario` con `diaSemana` "MONDAY", `apertura` "09:00" y `cierre` "18:00"
- **THEN** la respuesta es 200 con 09:00-18:00 y el lunes no conserva la franja anterior ni tiene dos franjas

#### Scenario: Sin consultorio configurado

- **WHEN** se envía `PUT /api/consultorio/horario` sobre una base sin consultorio
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

#### Scenario: Apertura igual al cierre

- **WHEN** se envía `PUT /api/consultorio/horario` con `apertura` "10:00" y `cierre` "10:00"
- **THEN** la respuesta es 422 con `codigo` igual a `HORARIO_INVALIDO`

#### Scenario: Apertura posterior al cierre

- **WHEN** se envía `PUT /api/consultorio/horario` con `apertura` "20:00" y `cierre` "08:00"
- **THEN** la respuesta es 422 con `codigo` igual a `HORARIO_INVALIDO`

### Requirement: Cerrar un día con franja previa

El sistema SHALL permitir cerrar un día enviando `apertura` y `cierre` `null` en `PUT /api/consultorio/horario`, eliminando solo la franja de ese día sin borrar otros datos. Si se envía solo uno de los dos valores, SHALL responder 400 con `codigo` igual a `ENTRADA_INVALIDA`. Un día cerrado MUST hacer que los turnos de ese día se rechacen por `FUERA_DE_HORARIO`, según la spec de `turnos`.

#### Scenario: Cerrar lunes con franja previa

- **GIVEN** el lunes tiene la franja 08:00-20:00
- **WHEN** se envía `PUT /api/consultorio/horario` con `diaSemana` "MONDAY", `apertura` null y `cierre` null
- **THEN** la respuesta es 200 con ambos campos en `null` y una consulta posterior muestra el lunes cerrado sin franja

#### Scenario: Solo un valor de la franja

- **WHEN** se envía `PUT /api/consultorio/horario` con `apertura` "08:00" y `cierre` null
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA` y el día conserva su franja anterior

### Requirement: Entrada inválida en la configuración

Los campos obligatorios y formatos de las solicitudes de configuración SHALL validarse antes de ejecutar cualquier regla de negocio, respondiendo 400 con `codigo` igual a `ENTRADA_INVALIDA` e indicando el campo inválido, conforme a la spec de `base-del-sistema`.

#### Scenario: Campo obligatorio ausente

- **WHEN** se envía `POST /api/prestaciones` sin `nombre`
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA` y menciona el campo `nombre`

#### Scenario: Día de la semana inválido

- **WHEN** se envía `PUT /api/consultorio/horario` con `diaSemana` "FUNDAY"
- **THEN** la respuesta es 400 con `codigo` igual a `ENTRADA_INVALIDA`
