# Preguntas Abiertas

## Inconsistencias detectadas

### IN-01: Thymeleaf + JWT
**Decisión A dice**: interfaz web con vistas Thymeleaf renderizadas en el servidor (DD-05).
**Decisión B dice**: autenticación con login de usuario y contraseña y JWT (ronda técnica, 4b).
**Impacto**: JWT es natural para APIs consumidas por otro cliente (SPA o móvil). Con Thymeleaf, el navegador no envía un header `Authorization` en cada navegación sin JavaScript adicional, y guardar el token en una cookie obliga a resolver CSRF de todos modos.
**Resolución propuesta**: form login con **sesión** de Spring Security para las vistas Thymeleaf. JWT **solo** para `/api/**`, si se decide exponer la API a otro cliente. Decidirlo en el change de autenticación, que no es el primero (DD-06).

### IN-02: Tamaño del equipo vs. la consigna
**La consigna dice**: "Individual o en grupo de hasta 3 integrantes".
**El equipo informó**: 6 personas.
**Impacto**: puede afectar la evaluación de "trazabilidad y participación".
**Resolución propuesta**: confirmar con la cátedra antes de la entrega.

### IN-03: Estructura de carpetas sugerida vs. Maven
**La consigna sugiere**: `src/` y `tests/` en la raíz.
**Maven usa**: `src/main/java` y `src/test/java`.
**Resolución propuesta**: seguir Maven (DD-07) y aclararlo en el README.

## Preguntas abiertas (priorizadas)

| ID | Prioridad | Pregunta | Bloquea | Decisor |
|---|---|---|---|---|
| PA-02 | Media | ¿El horario de atención es único o varía por día? Supuesto: una franja por día (SU-03) | Change de configuración | Product Owner |
| PA-05 | Baja | ¿Los feriados bloquean la agenda? Supuesto: no en la v1 (SU-06) | Etapas posteriores | Product Owner |
| PA-08 | Baja | Si Argentina volviera a aplicar horario de verano, ¿cómo se tratan los turnos en el cambio de hora? | — | Equipo técnico |
| PA-09 | Baja | IN-01: ¿sesión o JWT para las vistas? | Change de autenticación | Equipo técnico |

## Datos "No evidenciado" del Discovery

Datos que la investigación de mercado (2026-11-20) no pudo respaldar con fuentes públicas. Conviene validarlos con demos (informe D.1) antes de usarlos como argumento comercial:

| Tema | Sistemas sin evidencia pública | Por qué importa |
|---|---|---|
| Precios | Bilog, Dentalink, Órbita, ClinIA, Curve, Dentidesk (solo cotización) | El diferencial "precio publicado" (DD-01) se apoya en esta opacidad |
| Prevención de solapamientos | Todos salvo Oclusar (profesional), Órbita (sillón), Odontosist (dentista) y Dentiqa (solo comercial) | Es la base del diferencial; si otros lo hacen sin documentarlo, el vacío es menor |
| Validación conjunta profesional + sillón | Ningún sistema relevado | Ídem; validar en demos de Órbita y Oclusar |
| Duración variable de turnos | Bilog (arrastrar sí), Oclusar, Dentalink | Referencia para RN-TU-01 |
| Sillones/boxes en la agenda | Bilog, Dentalink, Dentia, Odonto.ar | Referencia de modelo de datos |
| Normativa argentina declarada (Leyes 25.326 y 26.529) | Bilog, Órbita, Dentiqa | Referencia de cumplimiento |
| Backup | Bilog, Dentia, Órbita, Odonto.ar | Referencia de seguridad |
| Sitios sin evidencia legible | Doctoralia AR (sitio caído), Reservo (solo JavaScript) | Quedaron fuera de la comparación |
| Adopción real | Cifras como "+15.000 clientes" (Dentalink) o "+30.000 clínicas" (AgendaPro) son afirmaciones comerciales | No usarlas como dato de mercado sin verificar |

## Preguntas resueltas

| ID | Resolución | Cuándo / dónde |
|---|---|---|
| PA-01 | JDK 17 (Oracle 17.0.12) instalado en la máquina del líder; cada integrante instala el suyo (README). | 2026-11-20, apply de C-01 |
| PA-03 | El sobreturno saltea todo el chequeo del profesional, **margen incluido**; con el sillón y el paciente las reglas se aplican igual (SU-04 confirmado). | 2026-11-20, explore de C-02 |
| PA-04 | Puede marcar sobreturno cualquier rol que pueda crear turnos (SECRETARIA, ADMIN). La regla no chequea el rol; lo hace la autorización por ruta de C-05. | 2026-11-20, explore de C-02 |
| PA-06 | **Bloqueo pesimista** de sillón, profesional y paciente (en ese orden) dentro de la transacción de creación, antes de consultar choques (RN-TU-10). Se prueba con dos creaciones concurrentes. | 2026-11-20, explore de C-02 |
| PA-07 | El inicio va en **múltiplos de 5 minutos** y segundos en 0 (nueva RN-TU-11, `ENTRADA_INVALIDA`). SU-05 descartado. | 2026-11-20, explore de C-02 |
