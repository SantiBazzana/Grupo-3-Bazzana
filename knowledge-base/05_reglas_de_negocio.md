# Reglas de Negocio

Cada regla tiene un código único `RN-{DOMINIO}-{NN}`. Los errores de la API devuelven ese código en el campo `codigo`. Origen: reglas R1 a R8 del Discovery (`docs/discovery/discovery.md` §7), confirmadas por el usuario el 2026-11-20.

## Definiciones

- **Intervalo de un turno**: `[inicio, fin)`, semiabierto, con `fin = inicio + duración de la prestación`.
- **Margen** (`M`): 5 minutos, configurable (`turnos.margen-minutos`).
- **Turno activo**: turno en estado `RESERVADO`. Los `CANCELADO` no cuentan para ninguna regla de choque.
- **Choque con margen** entre un turno nuevo `N` y uno existente `E` sobre el mismo recurso:
  `N.inicio < E.fin + M` **y** `E.inicio < N.fin + M`.
  Ejemplo: si `E` termina a las 10:00, un `N` que empieza a las 10:05 es válido y uno que empieza a las 10:00 choca. Un inicio a las 10:04 no llega a esta regla: RN-TU-11 (múltiplos de 5) lo rechaza antes con `ENTRADA_INVALIDA`.
- **Superposición simple** (sin margen), usada para el paciente: `N.inicio < E.fin` **y** `E.inicio < N.fin`.
- **"Ahora"**: el instante del `Clock` del sistema, en la zona horaria del consultorio.

## Dominio: Turnos (RN-TU)

- **RN-TU-01, Duración por prestación**: el `fin` de un turno es `inicio + prestacion.duracionMinutos`. El cliente no envía el `fin`; si lo envía, se ignora. *(R1)*
- **RN-TU-02, Choque por profesional**: un turno no puede chocar con margen con otro turno activo del **mismo profesional**, salvo que sea sobreturno (RN-TU-04). Error `CHOQUE_PROFESIONAL` (409). *(R2)*
- **RN-TU-03, Sillón obligatorio y exclusivo**: todo turno ocupa exactamente un sillón activo. Un turno no puede chocar con margen con otro turno activo del **mismo sillón**, **ni siquiera si es sobreturno**. Error `CHOQUE_SILLON` (409); sin sillón: `SILLON_REQUERIDO` (422). *(R2, R3)*
- **RN-TU-04, Sobreturno**: un turno creado con `sobreturno = true` se exime **solo** de RN-TU-02. Sigue sujeto a RN-TU-03, RN-TU-05, RN-TU-06 y RN-TU-07. *(R4)*
- **RN-TU-05, Paciente sin superposición**: un paciente no puede tener dos turnos activos superpuestos (superposición simple, sin margen). Error `CHOQUE_PACIENTE` (409). *(R5)*
- **RN-TU-06, No en el pasado**: `inicio` tiene que ser posterior al "ahora". Error `TURNO_EN_PASADO` (422). *(R6)*
- **RN-TU-07, Dentro del horario de atención**: el intervalo `[inicio, fin)` completo tiene que caer dentro de la franja de atención del consultorio para ese día de la semana (`apertura <= inicio` y `fin <= cierre`). Un turno que termina después del cierre se rechaza. Error `FUERA_DE_HORARIO` (422). *(R6)*
- **RN-TU-08, Recursos activos**: el profesional, el sillón y la prestación tienen que existir y estar activos, y el paciente tiene que existir. Error `RECURSO_INEXISTENTE` (404) o `RECURSO_INACTIVO` (422).
- **RN-TU-09, Orden de validación**: si un turno viola varias reglas, se informa la primera en este orden: RN-TU-08 → RN-TU-11 → RN-TU-06 → RN-TU-07 → RN-TU-03 → RN-TU-02 → RN-TU-05. Así los tests son deterministas.
- **RN-TU-10, Atomicidad**: la validación de choques y la persistencia del turno ocurren en la misma transacción. Antes de consultar choques se **bloquean** (bloqueo pesimista) las filas del sillón, del profesional y del paciente, siempre en ese orden, para que dos creaciones concurrentes no dejen un choque persistido. *(PA-06, resuelta el 2026-11-20 en el explore de C-02.)*
- **RN-TU-11, Inicio en múltiplos de 5 minutos**: el `inicio` de un turno tiene minutos múltiplos de 5 y segundos en 0 (09:00, 09:05… se aceptan; 09:03 o 09:05:30 se rechazan). Error `ENTRADA_INVALIDA` (400). *(PA-07, resuelta el 2026-11-20 en el explore de C-02.)*
- **Aclaraciones del sobreturno** *(PA-03 y PA-04, resueltas el 2026-11-20)*: el sobreturno se exime de RN-TU-02 **completa, margen incluido**; con el sillón y el paciente las reglas se aplican igual. Puede marcarlo cualquier rol que pueda crear turnos (SECRETARIA y ADMIN); la regla de turnos no chequea el rol, eso lo hace la autorización por ruta (C-05).
- **Consultorio cerrado**: si el día no tiene franja de atención, o no hay consultorio configurado, el turno se rechaza con `FUERA_DE_HORARIO` (RN-TU-07).

## Dominio: Cancelación (RN-CA)

- **RN-CA-01, Anticipación mínima**: un turno se puede cancelar solo si faltan **al menos 6 horas** para su inicio (`ahora <= inicio - 6h`). Si faltan menos, se rechaza con `CANCELACION_TARDIA` (422). *(R7)*
- **RN-CA-02, Libera el horario**: al cancelar, el turno pasa a `CANCELADO`, se registra `canceladoEn`, y desde ese momento no cuenta para RN-TU-02, RN-TU-03 ni RN-TU-05. *(R7)*
- **RN-CA-03, Idempotencia de estado**: un turno ya cancelado no se puede volver a cancelar. Error `TURNO_YA_CANCELADO` (422).
- **RN-CA-04, Sin borrado físico**: los turnos no se eliminan; la cancelación es un cambio de estado, lo que deja trazabilidad.

## Dominio: Agenda (RN-AG)

- **RN-AG-01, Agenda del día**: la agenda de un profesional para una fecha devuelve sus turnos activos cuyo `inicio` cae en esa fecha (zona del consultorio), ordenados por `inicio`. Cada turno muestra paciente, prestación, sillón, horario y si es sobreturno.
- **RN-AG-02, Visibilidad del odontólogo**: un `ODONTOLOGO` solo puede consultar la agenda del profesional vinculado a su usuario.

## Dominio: Configuración (RN-CF)

- **RN-CF-01**: la duración de una prestación es un entero mayor que 0 y múltiplo de 5 minutos.
- **RN-CF-02**: la franja de atención cumple `apertura < cierre`; hay una franja por día de la semana como máximo.
- **RN-CF-03**: profesionales, sillones y prestaciones no se borran físicamente si tienen turnos; se desactivan (`activo = false`). Un recurso inactivo no se puede usar en turnos nuevos y sus turnos existentes se conservan.
- **RN-CF-04**: la matrícula del profesional, el nombre del sillón y el nombre de la prestación son únicos.

## Dominio: Notificación por WhatsApp (RN-WA)

- **RN-WA-01**: el link es `https://wa.me/{telefono}?text={mensaje URL-encoded}`, con el teléfono del paciente en formato internacional sin `+` ni espacios.
- **RN-WA-02**: el mensaje incluye fecha y hora del turno, profesional y prestación. No incluye datos clínicos.
- **RN-WA-03**: si el paciente no tiene teléfono cargado, el link no se genera. Error `PACIENTE_SIN_TELEFONO` (422).
- **RN-WA-04**: el sistema no envía mensajes; solo arma el link. El envío lo hace la secretaria a mano.

## Dominio: Autenticación y autorización (RN-AU)

- **RN-AU-01**: todas las rutas, salvo las públicas (`03_actores_y_roles.md`), requieren usuario autenticado.
- **RN-AU-02**: las contraseñas se almacenan con BCrypt; nunca en texto plano ni en logs.
- **RN-AU-03**: los permisos se aplican por rol según la matriz RBAC (`03_actores_y_roles.md`).
- **RN-AU-04**: un usuario con rol `ODONTOLOGO` debe estar vinculado a un `Profesional`.

## Dominio: Datos personales (RN-DP)

- **RN-DP-01**: nunca se almacenan datos reales de pacientes en el repositorio, los ejemplos, el seed, los tests ni las capturas. Todos son ficticios.
- **RN-DP-02**: los datos de pacientes son datos personales (y de salud en etapas posteriores) bajo la **Ley 25.326**; la historia clínica futura queda bajo la **Ley 26.529**. El acceso se limita por rol (RN-AU-03).
- **RN-DP-03**: los logs no deben incluir DNI, teléfono ni nombre completo de pacientes.

## Excepciones globales

- **RN-GL-01, Zona horaria**: todas las fechas y horas de negocio se interpretan en `America/Argentina/Buenos_Aires` (R8). La API recibe y devuelve `LocalDateTime` en esa zona (ISO-8601 sin offset). Argentina no aplica horario de verano al 2026; si cambiara, ver PA-08.
- **RN-GL-02, Reloj inyectable**: ninguna regla usa `LocalDateTime.now()` directamente; todas usan el `Clock` del sistema, para poder testear RN-TU-06 y RN-CA-01.
