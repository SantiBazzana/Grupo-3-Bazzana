# Modelo de Datos

## Dominios

| Dominio | Entidades | Descripción |
|---|---|---|
| Configuración | `Consultorio`, `HorarioAtencion`, `Profesional`, `Sillon`, `Prestacion` | Recursos que el administrador define y la agenda usa |
| Pacientes | `Paciente` | Datos mínimos de contacto (sensibles: Ley 25.326) |
| Agenda | `Turno` | Reserva de un profesional + sillón + paciente + prestación en un intervalo |
| Acceso | `Usuario` | Credenciales y rol; un `ODONTOLOGO` se vincula a un `Profesional` |

## ERD

```
Consultorio 1 ──── * HorarioAtencion (día de semana, apertura, cierre)

Usuario * ──── 0..1 Profesional     (solo para rol ODONTOLOGO)

Profesional 1 ──── * Turno * ──── 1 Sillon
                     │  *
                     │
          Paciente 1 ┘  └─ * ──── 1 Prestacion
```

Un `Turno` referencia exactamente un `Profesional`, un `Sillon`, un `Paciente` y una `Prestacion`.

## Entidades

### Consultorio
- `id` (Long, PK), `nombre` (String, not null), `zonaHoraria` (String, default `America/Argentina/Buenos_Aires`).
- La v1 tiene **un solo consultorio** (sin multisede). La entidad existe para no rehacer el modelo después.

### HorarioAtencion
- `id` (Long, PK), `consultorioId` (FK), `diaSemana` (`DayOfWeek`), `apertura` (`LocalTime`), `cierre` (`LocalTime`).
- Constraint: `apertura < cierre`. Único por (`consultorioId`, `diaSemana`).
- **Decisión** (SU-03, confirmada el 2026-11-21, PA-02): una franja por día; los días sin registro están cerrados. Si el consultorio usa un único horario para toda la semana, se cargan los mismos valores en cada día. La administración del horario está en la spec `configuracion-consultorio` (C-03).

### Profesional
- `id` (Long, PK), `nombre` (String), `apellido` (String), `matricula` (String, única), `telefono` (String, opcional), `activo` (boolean, default true).

### Sillon
- `id` (Long, PK), `nombre` (String, único; por ejemplo "Sillón 1"), `activo` (boolean).

### Prestacion
- `id` (Long, PK), `nombre` (String, único; por ejemplo "Control", "Conducto"), `duracionMinutos` (int, > 0, múltiplo de 5), `activa` (boolean).
- La duración es **fija por prestación** (RN-TU-01).

### Paciente
- `id` (Long, PK), `nombre`, `apellido` (String, not null), `dni` (String, único), `telefono` (String, formato E.164 sin `+`, por ejemplo `5491100000000`), `email` (opcional).
- Datos sensibles: nunca reales en el repositorio (RN-DP-01).

### Turno
- `id` (Long, PK)
- `profesionalId` (FK not null), `sillonId` (FK not null, RN-TU-03), `pacienteId` (FK not null), `prestacionId` (FK not null)
- `inicio` (`LocalDateTime`, en la zona del consultorio)
- `fin` (`LocalDateTime`) = `inicio + prestacion.duracionMinutos`. Se calcula al crear el turno y se persiste para consultar choques por rango.
- `sobreturno` (boolean, default false)
- `estado` (enum `TurnoEstado`: `RESERVADO`, `CANCELADO`; en etapas posteriores se agregan `ASISTIO` y `AUSENTE`)
- `canceladoEn` (`LocalDateTime`, nullable), `creadoEn` (`LocalDateTime`), `creadoPor` (usuario)
- **Índices**: (`profesionalId`, `inicio`), (`sillonId`, `inicio`), (`pacienteId`, `inicio`). Son los que usan las consultas de choque, que buscan turnos `RESERVADO` cuyo intervalo, extendido por el margen, se superpone con el nuevo.
- Constraint de aplicación: `fin > inicio`.

### Usuario
- `id` (Long, PK), `username` (String, único), `passwordHash` (BCrypt), `rol` (enum `ADMIN | SECRETARIA | ODONTOLOGO`), `profesionalId` (FK nullable, obligatorio si el rol es `ODONTOLOGO`), `activo`.

## Seed data inicial (perfil `dev`, datos ficticios)

- Consultorio "Consultorio Demo", horario de lunes a viernes de 08:00 a 20:00.
- Sillones: "Sillón 1", "Sillón 2".
- Profesionales: "Ana Ejemplo" (MP-0001), "Bruno Prueba" (MP-0002).
- Prestaciones: Control (30 min), Limpieza (45 min), Conducto (60 min), Extracción (40 min).
- Pacientes: 3 ficticios ("Paciente Uno"…), con DNI `00000001`… y teléfono `5491100000001`…
- Usuarios: `admin`, `secretaria`, `ana` (odontóloga). Las contraseñas de seed se leen de variables de entorno o se documentan como valores de desarrollo; **nunca** son credenciales reales.
