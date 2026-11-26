# Flujos Principales

## Flujo 1: Crear turno (núcleo)

**Disparador**: un paciente pide un turno por teléfono o WhatsApp.
**Actor**: Secretaria (o Administrador).

**Pasos**:
1. La secretaria elige paciente, profesional, sillón, prestación, hora de inicio y, si corresponde, marca "sobreturno".
2. El controlador (`POST /api/turnos` o el formulario Thymeleaf) valida el formato de la entrada (Bean Validation) y llama al caso de uso `CrearTurno`.
3. `CrearTurno`, dentro de una transacción:
   1. Carga el profesional, el sillón, la prestación y el paciente, y verifica que existan y estén activos (RN-TU-08).
   2. Calcula `fin = inicio + duración` (RN-TU-01).
   3. Verifica que no sea en el pasado contra el `Clock` (RN-TU-06).
   4. Verifica que `[inicio, fin)` caiga dentro del horario de atención del día (RN-TU-07).
   5. Busca turnos activos del **sillón** que choquen con margen y, si hay alguno, devuelve `CHOQUE_SILLON` (RN-TU-03).
   6. Si **no** es sobreturno, busca turnos activos del **profesional** que choquen con margen y, si hay alguno, devuelve `CHOQUE_PROFESIONAL` (RN-TU-02 y RN-TU-04).
   7. Busca turnos activos del **paciente** superpuestos y, si hay alguno, devuelve `CHOQUE_PACIENTE` (RN-TU-05).
   8. Persiste el turno en estado `RESERVADO`.
4. La respuesta es `201` con el turno creado (incluye `fin`). Si hubo error, la respuesta lleva el código de la regla.

**Diagrama de secuencia**:
```
Secretaria → Controller → CrearTurno → Repositorios (JPA/H2)
                              │── cargar recursos ──────►│
                              │── validar pasado/horario (Clock, Horario)
                              │── choques sillón ───────►│
                              │── choques profesional ──►│ (si no es sobreturno)
                              │── choques paciente ─────►│
                              │── guardar ──────────────►│
            ◄── 201 Turno ────┘
```

**Casos de error**:
| Caso | Código | HTTP |
|---|---|---|
| Recurso inexistente o inactivo | `RECURSO_INEXISTENTE` / `RECURSO_INACTIVO` | 404 / 422 |
| Inicio en el pasado | `TURNO_EN_PASADO` | 422 |
| Fuera del horario de atención | `FUERA_DE_HORARIO` | 422 |
| Sillón ocupado (con margen) | `CHOQUE_SILLON` | 409 |
| Profesional ocupado (con margen, sin sobreturno) | `CHOQUE_PROFESIONAL` | 409 |
| Paciente con turno superpuesto | `CHOQUE_PACIENTE` | 409 |

**Ejemplos de borde** (con margen de 5 minutos y un turno existente de 09:30 a 10:00 en el Sillón 1 con Ana):
- Nuevo turno 10:05 a 10:35, Sillón 1, Ana → **aceptado**.
- Nuevo turno 10:00 a 10:30, Sillón 1 → **rechazado** `CHOQUE_SILLON` (empieza justo cuando termina el otro, sin los 5 minutos de margen).
- Nuevo turno 10:04 a 10:34 → **rechazado** `ENTRADA_INVALIDA` (RN-TU-11: el inicio no es múltiplo de 5, y esa regla se valida antes que el sillón).
- Nuevo turno 09:45 a 10:15, Sillón 2, Ana, sobreturno → **aceptado** (choca solo el profesional).
- Nuevo turno 09:45 a 10:15, Sillón 1, Bruno, sobreturno → **rechazado** `CHOQUE_SILLON`.

## Flujo 2: Cancelar turno

**Disparador**: el paciente avisa que no puede venir.
**Actor**: Secretaria.

**Pasos**:
1. La secretaria busca el turno y pide la cancelación (`POST /api/turnos/{id}/cancelacion`).
2. `CancelarTurno` verifica que el turno exista y esté `RESERVADO` (RN-CA-03).
3. Verifica que `ahora <= inicio - 6h` (RN-CA-01).
4. Cambia el estado a `CANCELADO` y registra `canceladoEn` (RN-CA-02 y RN-CA-04).
5. Responde `200` con el turno actualizado. El horario queda libre para turnos nuevos.

**Casos de error**: `TURNO_YA_CANCELADO` (422), `CANCELACION_TARDIA` (422), turno inexistente (404).

## Flujo 3: Consultar la agenda del día

**Disparador**: el odontólogo empieza su jornada.
**Actor**: Odontólogo (o Secretaria o Administrador).

**Pasos**:
1. Pide `GET /api/agenda?profesionalId=X&fecha=AAAA-MM-DD`, o abre la vista "Mi agenda".
2. La seguridad verifica que un `ODONTOLOGO` solo pida la agenda de su profesional (RN-AG-02); si no, responde 403.
3. Se devuelven los turnos `RESERVADO` del día, ordenados por inicio (RN-AG-01).

## Flujo 4: Avisar al paciente por WhatsApp

**Actor**: Secretaria.

**Pasos**:
1. Desde el turno, pide `GET /api/turnos/{id}/whatsapp-link`.
2. El sistema arma el mensaje (fecha, hora, profesional, prestación) y el link `https://wa.me/<tel>?text=<mensaje>` (RN-WA-01 y RN-WA-02).
3. La secretaria abre el link y envía el mensaje desde su WhatsApp (RN-WA-04).

**Caso de error**: paciente sin teléfono → `PACIENTE_SIN_TELEFONO` (422).

## Flujo 5: Inicio de sesión

**Actor**: cualquier usuario del consultorio.

**Pasos**:
1. Envía usuario y contraseña (vista `/login` o `POST /api/auth/login`).
2. Spring Security valida contra el hash BCrypt (RN-AU-02).
3. Si es válido, se emite una sesión o un token, según la resolución de IN-01. Si no, 401.
4. Las siguientes solicitudes se autorizan por rol (RN-AU-03).
