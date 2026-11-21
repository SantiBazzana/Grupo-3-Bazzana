# Decisiones y Supuestos

## Decisiones documentadas

### DD-01: Foco del producto: validación conjunta de profesional y sillón
**Decisión**: el núcleo del MVP es una agenda que rechaza choques por profesional **y** por sillón.
**Contexto**: el Discovery relevó 19 sistemas; ninguno documenta públicamente la validación conjunta (informe, sección C.3).
**Alternativas consideradas**: competir con IA en WhatsApp (tendencia dominante: Dentia, Órbita, ClinIA); competir con integración fiscal y de obras sociales.
**Justificación**: es un vacío verificado, cabe en el plazo y se puede especificar y testear.
**Trade-offs aceptados**: sin IA, sin recordatorios automáticos ni cobros en la v1, que son estándar del mercado.

### DD-02: Stack Java 17 + Spring Boot 4.2 + Maven
**Decisión**: backend en Java 17 con Spring Boot 4.2 y Maven Wrapper.
**Revisión (2026-11-20, apply de C-01)**: la ronda técnica eligió Spring Boot 3, pero Spring Initializr ya solo ofrece 4.x y la última 3.x (3.5.16) no recibe más parches. El líder aprobó pasar a **4.2.0**. Detalle de las diferencias para el equipo en `openspec/changes/scaffold-proyecto/design.md` (D1).
**Contexto**: elección del equipo en la ronda técnica de la KB (2026-11-20).
**Alternativas consideradas**: Node + TypeScript + Fastify (ya instalado en la máquina del líder); Python + FastAPI.
**Justificación**: preferencia del equipo; ecosistema maduro para web + seguridad + JPA.
**Trade-offs aceptados**: hay que instalar el JDK 17 en cada máquina (PA-01); arranque y tests más lentos que en Node.

### DD-03: Reglas de agenda en un dominio Java puro
**Decisión**: las reglas RN-TU y RN-CA viven en `domain`, sin dependencias de Spring.
**Justificación**: tests unitarios rápidos, uno por escenario del spec, y un contrato claro para la fase Apply de OPSX.
**Trade-offs**: algo más de código (mapeo entre entidad JPA y modelo de dominio, o anotaciones JPA en el modelo).

### DD-04: Persistencia H2 en memoria con Spring Data JPA
**Decisión**: H2 en memoria por defecto, con PostgreSQL como destino futuro.
**Contexto**: el equipo eligió persistencia en memoria (3a) y luego H2 con JPA (8b).
**Justificación**: no hay que instalar una base de datos; el README se puede correr "solo clonando"; el paso a Postgres es un cambio de configuración.
**Trade-offs**: los datos se pierden al reiniciar; las diferencias de dialecto con Postgres se descubren tarde.

### DD-05: Interfaz web con Thymeleaf
**Decisión**: vistas renderizadas en el servidor con Thymeleaf, en el mismo proyecto.
**Alternativas consideradas**: SPA en React o Angular.
**Justificación**: un solo proyecto y un solo toolchain para 6 personas en 5 días.
**Trade-offs**: interfaz menos rica; la API REST se mantiene igual para tests y para un futuro frontend.

### DD-06: Orden de construcción: dominio de agenda primero
**Decisión**: el primer change OPSX es "crear turno sin solapamientos" (dominio + API). Login con JWT y vistas Thymeleaf van en changes posteriores.
**Contexto**: entrega el 2026-11-25; la consigna evalúa un solo change completo.
**Justificación**: el riesgo principal del Discovery es no llegar con el ciclo completo.

### DD-07: Tests en `src/test/java`
**Decisión**: se sigue la convención de Maven en lugar de una carpeta `tests/` en la raíz.
**Justificación**: Maven y Spring Boot la esperan; el README lo aclara para la corrección.

### DD-08: Margen con intervalo semiabierto
**Decisión**: hay choque si `N.inicio < E.fin + M` y `E.inicio < N.fin + M`, con `M = 5 min`.
**Justificación**: traduce R2 de forma testeable: 10:05 es válido y 10:04 choca después de un turno que termina a las 10:00.

### DD-09: WhatsApp por click-to-chat
**Decisión**: el MVP solo genera links `wa.me`; el envío lo hace la secretaria.
**Justificación**: sin costo, sin credenciales ni aprobación de Meta. Oclusar usa el mismo enfoque (informe, fila 3).
**Trade-offs**: no hay recordatorios automáticos (es el estándar del mercado; queda para la próxima etapa).

## Supuestos inferidos

### SU-01: El doble turno es el dolor principal
**Supuesto**: los consultorios objetivo sufren dobles turnos por profesional y por sillón.
**Origen**: respuesta del equipo en el Discovery (P2 = a + c).
**Riesgo si es falso**: el diferencial no mueve la aguja frente a la IA en WhatsApp.
**Cómo validar**: entrevistar a 3 consultorios o pedir demo a Órbita y Oclusar (informe D.1).

### SU-02: Las funcionalidades de los competidores son reales
**Supuesto**: lo marcado como "comprobado" en el informe existe.
**Origen**: lectura de sitios oficiales; buena parte es afirmación comercial o maqueta.
**Cómo validar**: verificación de fuentes del equipo (informe, sección "Verificación de fuentes") y demos.

### SU-03: Un único horario de atención por día para todo el consultorio — **CONFIRMADO**
**Supuesto**: hay una franja por día de la semana, sin horarios por profesional (que quedan para etapas posteriores).
**Origen**: el Discovery dejó la pregunta abierta; el horario por profesional se marcó como posterior.
**Riesgo si es falso**: el modelo `HorarioAtencion` habría que extenderlo.
**Validación**: adoptado en C-03 (spec `configuracion-consultorio`) y confirmado por el líder como Product Owner el 2026-11-21 (PA-02).

### SU-04: Un sobreturno ignora el margen con el profesional — **CONFIRMADO**
**Supuesto**: como el sobreturno se exime de RN-TU-02 completo, el margen de 5 minutos con el profesional tampoco aplica. Con el sillón, el margen sí aplica.
**Origen**: interpretación de R4 (pregunta abierta del Discovery).
**Validación**: confirmado por el líder el 2026-11-20 en el explore de C-02 (PA-03).

### SU-05: Turnos en cualquier minuto — **DESCARTADO**
**Supuesto original**: el inicio de un turno puede ser cualquier minuto.
**Resolución**: el 2026-11-20, en el explore de C-02, el líder decidió lo contrario (PA-07): el inicio va en múltiplos de 5 minutos. Ahora es la regla **RN-TU-11** de `05_reglas_de_negocio.md`.

### SU-06: Sin feriados en el MVP
**Supuesto**: los feriados no bloquean la agenda en la v1 (Dentidesk sí los bloquea).
**Cómo validar**: pregunta PA-05.
