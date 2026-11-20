# Funcionalidades

Organizadas por **épica** y luego por **historia de usuario** (US-NNN). La columna "Etapa" sigue el MVP de la sección D del informe de Discovery.

| Épica | Historias | Etapa |
|---|---|---|
| E1 Configuración del consultorio | US-001 a US-004 | MVP |
| E2 Gestión de turnos | US-010, US-011 | MVP (núcleo) |
| E3 Agenda | US-020 | MVP |
| E4 Aviso por WhatsApp | US-030 | MVP |
| E5 Acceso y seguridad | US-040, US-041 | MVP |
| E6 Pacientes | US-050 | MVP |
| E7 Backlog | US-1xx | Próxima etapa / posteriores |

## Épica 1: Configuración del consultorio

### US-001: Cargar prestaciones con duración
**Como** administrador **quiero** cargar prestaciones con su duración en minutos **para** que la agenda calcule el fin de cada turno.
**Criterios de aceptación**:
- [ ] Puedo crear una prestación con nombre único y duración múltiplo de 5 mayor que 0.
- [ ] Una duración inválida (0, negativa o no múltiplo de 5) se rechaza.
- [ ] Puedo desactivar una prestación; no aparece para turnos nuevos.
**Reglas relacionadas**: RN-CF-01, RN-CF-03, RN-CF-04

### US-002: Cargar sillones
**Como** administrador **quiero** dar de alta los sillones del consultorio **para** que cada turno ocupe uno.
**Criterios**: nombre único; se puede desactivar sin perder los turnos existentes.
**Reglas relacionadas**: RN-CF-03, RN-CF-04, RN-TU-03

### US-003: Cargar profesionales
**Como** administrador **quiero** dar de alta odontólogos con matrícula **para** asignarles turnos.
**Criterios**: matrícula única; se puede desactivar; se puede vincular a un usuario `ODONTOLOGO`.
**Reglas relacionadas**: RN-CF-03, RN-CF-04, RN-AU-04

### US-004: Definir horario de atención
**Como** administrador **quiero** definir el horario de atención por día de la semana **para** que no se den turnos con el consultorio cerrado.
**Criterios**: una franja por día con `apertura < cierre`; los días sin franja quedan cerrados.
**Reglas relacionadas**: RN-CF-02, RN-TU-07

## Épica 2: Gestión de turnos (núcleo del producto)

### US-010: Crear turno sin solapamientos
**Como** secretaria **quiero** dar un turno eligiendo paciente, profesional, sillón, prestación y hora de inicio **para** que quede reservado sin chocar con otros turnos.
**Criterios de aceptación**:
- [ ] El `fin` se calcula como inicio + duración de la prestación.
- [ ] Si el profesional tiene otro turno activo que choca, considerando el margen de 5 minutos, se rechaza con `CHOQUE_PROFESIONAL`.
- [ ] Si el sillón tiene otro turno activo que choca, considerando el margen de 5 minutos, se rechaza con `CHOQUE_SILLON`.
- [ ] Un turno que empieza 5 minutos o más después de que termina otro del mismo profesional o sillón se acepta. Uno que empieza 4 minutos después se rechaza.
- [ ] Marcado como sobreturno, se acepta aunque choque con el profesional, pero **no** si choca con el sillón.
- [ ] Si el paciente ya tiene un turno activo superpuesto, se rechaza con `CHOQUE_PACIENTE`.
- [ ] Un turno en el pasado se rechaza con `TURNO_EN_PASADO`.
- [ ] Un turno que empieza antes de la apertura o termina después del cierre se rechaza con `FUERA_DE_HORARIO`.
- [ ] Un turno cancelado no bloquea un turno nuevo en ese horario.
**Reglas relacionadas**: RN-TU-01 a RN-TU-11, RN-GL-01, RN-GL-02

### US-011: Cancelar turno
**Como** secretaria **quiero** cancelar un turno **para** liberar el horario.
**Criterios de aceptación**:
- [ ] Con 6 horas o más de anticipación, el turno pasa a `CANCELADO` y el horario queda libre.
- [ ] Con menos de 6 horas, se rechaza con `CANCELACION_TARDIA`.
- [ ] Un turno ya cancelado no se vuelve a cancelar (`TURNO_YA_CANCELADO`).
**Reglas relacionadas**: RN-CA-01 a RN-CA-04

## Épica 3: Agenda

### US-020: Ver agenda del día
**Como** odontólogo **quiero** ver mi agenda del día **para** organizarme.
**Criterios**:
- [ ] Muestra los turnos activos del día ordenados por hora, con paciente, prestación, sillón y si es sobreturno.
- [ ] Un odontólogo no puede ver la agenda de otro.
- [ ] La secretaria y el administrador pueden ver la de cualquiera.
**Reglas relacionadas**: RN-AG-01, RN-AG-02

## Épica 4: Aviso por WhatsApp

### US-030: Link de WhatsApp del turno
**Como** secretaria **quiero** obtener un link de WhatsApp con el mensaje del turno ya escrito **para** avisarle al paciente sin tipear.
**Criterios**:
- [ ] El link tiene la forma `https://wa.me/<telefono>?text=<mensaje>`, con el mensaje URL-encoded.
- [ ] El mensaje incluye fecha, hora, profesional y prestación, sin datos clínicos.
- [ ] Si el paciente no tiene teléfono, se informa `PACIENTE_SIN_TELEFONO`.
**Reglas relacionadas**: RN-WA-01 a RN-WA-04

## Épica 5: Acceso y seguridad

### US-040: Iniciar sesión
**Como** usuario del consultorio **quiero** iniciar sesión con usuario y contraseña **para** acceder según mi rol.
**Criterios**: credenciales inválidas → 401; contraseñas guardadas con BCrypt; mecanismo de sesión o token según IN-01.
**Reglas relacionadas**: RN-AU-01, RN-AU-02

### US-041: Permisos por rol
**Como** administrador **quiero** que cada rol acceda solo a lo suyo **para** proteger los datos de los pacientes.
**Criterios**: se cumple la matriz RBAC de `03_actores_y_roles.md`; un acceso no permitido devuelve 403.
**Reglas relacionadas**: RN-AU-03, RN-DP-02

## Épica 6: Pacientes

### US-050: Alta de paciente
**Como** secretaria **quiero** registrar un paciente con nombre, DNI y teléfono **para** poder darle turnos.
**Criterios**: DNI único; teléfono en formato internacional; todos los ejemplos con datos ficticios.
**Reglas relacionadas**: RN-DP-01, RN-DP-03

## Épica 7: Backlog (fuera del MVP)

| ID | Historia | Etapa | Origen (informe D) |
|---|---|---|---|
| US-101 | Recordatorio automático 24 h antes con confirmación por WhatsApp Business API | Próxima | Diferenciador del mercado (Dentia, Órbita, SMILE) |
| US-102 | Ficha clínica y odontograma | Próxima | Estándar de mercado |
| US-103 | Cobro, seña con Mercado Pago y factura ARCA | Próxima | Diferencial local (Dentia, Oclusar) |
| US-110 | Reprogramar turno, revalidando choques y excluyendo el propio turno | Posterior | — |
| US-111 | Bloqueos y horario laboral por profesional | Posterior | Oclusar, Dentiqa |
| US-112 | Registro de asistencia y ausentes | Posterior | Bilog, Odontosist |
| US-113 | Reserva del paciente por link | Posterior | Odonto.ar, Dentalink |
| US-114 | Lista de espera para cubrir cancelaciones | Posterior | OdontoPlus, Open Dental |
| US-115 | Obras sociales y prepagas | Posterior | Bilog, ClinIA |
| US-116 | Multisede | Posterior | Órbita, Dentalink |
