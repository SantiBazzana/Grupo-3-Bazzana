# Design

## Context

Ver `proposal.md` para la motivación y `specs/configuracion-consultorio/spec.md` para el contrato observable. C-01 y C-02 ya están mergeados y archivados: en `domain/model` están `Profesional`, `Sillon`, `Prestacion`, `HorarioAtencion` y `Consultorio` con sus invariantes RN-CF-01..04; en `domain/port` están los repositorios con `existsByMatricula`, `existsByNombre`, `findByConsultorioIdAndDiaSemana`, `save` y `findAll`; `CodigoError`, `ManejadorGlobalDeErrores`, el seed `DatosDeEjemploDev` y las fixtures `DatosDePrueba`/`RelojDePrueba` ya existen. C-03 **solo agrega** `application/configuracion/**`, `web/api/configuracion/**` y sus tests espejo. Cualquier necesidad en archivos compartidos se pide al líder (E1); no se duplica el seed (ya reclamado por `base-del-sistema`). Stack fijo: Java 17, Spring Boot 4.2.0, Maven Wrapper, H2; no se suben versiones ni se agregan dependencias (J5).

## Goals / Non-Goals

**Goals:**

- Exponer casos de uso y una API REST para alta, edición, listado y baja lógica de los tres recursos configurables, más lectura y reemplazo del horario por día.
- Apoyarse en las invariantes existentes del modelo en lugar de redefinirlas, y traducir sus excepciones a `ProblemDetail` con `codigo` del catálogo existente (J2).
- Mantener la frontera de API con DTOs/records: ninguna entidad JPA sale por HTTP; lógica de negocio Java puro en casos de uso, sin anotaciones Spring/JPA en `domain/rules` (J1).
- Inyección por constructor en todos los componentes nuevos (J3), con `@Transactional` en el límite del caso de uso.
- Un test por escenario del spec con ciclo TDD RED → GREEN → TRIANGULATE → REFACTOR (J4).

**Non-Goals:**

- No implementar RBAC ni poner anotaciones de seguridad: ADMIN=CLMB y SECRETARIA/ODONTOLOGO=solo lectura es propiedad de C-05.
- No inyectar ni consultar `Clock`: la configuración no depende del "ahora" (D4).
- No vincular usuarios `ODONTOLOGO` a profesionales (US-003 efectivo queda para C-05); no editar `Usuario` ni `UsuarioRepository`.
- No borrar físicamente recursos; no duplicar el seed; no cambiar reglas RN-TU/RN-CA ni el catálogo de errores.
- No crear pantallas, tokens CSS ni assets visuales.

## Decisions

### (a) PA-02: adoptar SU-03 (franja única por día, día sin franja = cerrado)

**Decisión:** se asume una franja por consultorio y día de la semana; si no hay franja, el día está cerrado. `apertura == cierre` o `apertura > cierre` se rechaza con `HORARIO_INVALIDO` (422) apoyándose en `HorarioAtencion.validarFranja` / `cambiarFranja`. No se modifica ninguna RN: PA-02 sigue pendiente de confirmación del Product Owner, pero se registra como decisión asumida por este change; si el PO decide horarios por profesional, se vuelve a `propose` (D3, E5). **Alternativa rechazada:** modelar un "cierre" como franja con `apertura == cierre`, porque contradice la spec vigente de `turnos` y la invariante RN-CF-02.

### (b) Cerrar un día: interfaz de repositorio propia de C-03 (Spring Data)

**Decisión:** el puerto compartido `HorarioAtencionRepository` no expone `delete`/`deleteAll` y está protegido por E1. Se declara, dentro de `application/configuracion/` (paquete `persistencia`), una interfaz Spring Data propia y acotada, por ejemplo `RepositorioDeHorarioDeConfiguracion extends Repository<HorarioAtencion, Long>`, con `findByConsultorioIdAndDiaSemana(Long, DayOfWeek)` y `void delete(HorarioAtencion)`. El caso de uso `ReemplazarHorario` usa el puerto compartido para leer y esta interfaz solo para quitar la franja del día cuando `PUT` recibe `apertura` y `cierre` en `null`; la transacción y el límite `@Transactional` quedan en el caso de uso. No se asume que existe `delete` en el puerto compartido. **Justificación:** evita tocar un archivo de C-01 (E1), no bloquea al equipo esperando un PR del líder y mantiene el borrado confinado a C-03; Spring Data detecta interfaces bajo el paquete raíz `ar.odonto.turnos`. **Alternativa considerada:** pedir al líder que amplíe `HorarioAtencionRepository` con `delete` vía PR `chore(shared)`; es más limpio en capas pero introduce dependencia inter-equipo y retrasa la ola 1, así que se descarta salvo revisión del líder.

### (c) US-003: profesional vinculable en el modelo, vinculación efectiva en C-05

**Decisión:** C-03 da de alta/edita/listar/desactiva profesionales con matrícula única y expone su `id` para que C-05 lo relacione con un usuario. `Usuario` no tiene `vincularProfesional()` y `UsuarioRepository` no tiene `findByProfesionalId`; ambos son compartidos y editarlos viola E1. Por eso no se agrega ningún endpoint ni campo de vinculación, y la respuesta del profesional no expone usuario ni credenciales. Implementar algo más sería salirse de la spec aprobada (E5). **Alternativa rechazada:** agregar un campo `usuarioId` al DTO y resolverlo con reflexión o consultas ad hoc en C-03.

### (d) Unicidad en edición: chequear solo si el valor cambió

**Decisión:** `ProfesionalRepository.existsByMatricula`, `SillonRepository.existsByNombre` y `PrestacionRepository.existsByNombre` no excluyen el id actual, así que un `PUT` que repite el valor propio daría un falso `DATO_DUPLICADO` (409). Cada caso de uso de edición compara el valor recibido (normalizado) con el de la entidad cargada y solo invoca `existsBy...` si hubo cambio. La matrícula es inmutable en el modelo (`Profesional.actualizar` no la modifica): si el request la envía distinta se rechaza con `ENTRADA_INVALIDA` (400) antes que cualquier consulta de unicidad. La restricción `unique` de la base sigue siendo la última línea de defensa ante carreras, sin códigos nuevos. **Alternativa rechazada:** declarar `existsByNombreAndIdNot` / `existsByMatriculaAndIdNot` en interfaces propias de C-03, que duplicaría los puertos compartidos y añadiría más repositorios Spring Data sin beneficio observable.

### Estructura y fronteras

- `application/configuracion/`: casos de uso (`AltaPrestacion`, `EditarPrestacion`, `ListarPrestaciones`, equivalentes para sillones y profesionales, `ConsultarHorario`, `ReemplazarHorario`) con comandos/records de entrada y vistas de salida; `persistencia/` con la interfaz de la decisión (b).
- `web/api/configuracion/`: controladores y records de request/response con Bean Validation (`@Valid`), sin lógica de negocio; mapean vistas a DTOs y dejan que `ManejadorGlobalDeErrores` produzca el `ProblemDetail`.
- Sin `Clock` ni `LocalDateTime.now()`; sin anotaciones `@PreAuthorize`/`@Secured`; sin apertura de la API a `/api/v1` todavía (convención actual `/api`, según C-01 y C-02).
- Errores: se reutilizan `DURACION_INVALIDA` (422), `HORARIO_INVALIDO` (422), `DATO_DUPLICADO` (409), `RECURSO_INEXISTENTE` (404) y `ENTRADA_INVALIDA` (400) del catálogo existente; no se crean códigos nuevos (sería cambio compartido).

### UI Mockups

- `Decision: no (no UI)`. La política visual del proyecto está `deferred` y C-03 no tiene pantallas: C-09 `ui-base-y-gestion` es el primer change visual y resolverá la política una sola vez. No se crea `design.md` de mockups ni se invoca `design-system` aquí.

## Risks / Trade-offs

- [PA-02 sin confirmar del PO → el modelo de "día cerrado" podría cambiar] → Mitigación: decisión (a) explicada en proposal/design/spec; si cambia, volver a `propose` antes de tocar código.
- [Dos repositorios Spring Data para `HorarioAtencion` → posible confusión o bean duplicado] → Mitigación: interfaz propia package-private, methods mínimos, sin anotaciones de consulta, y tests de API que verifican el comportamiento observable, no el número de beans.
- [Falso `DATO_DUPLICADO` al editar → regresión visible en la UI futura] → Mitigación: decisión (d) con test de "editar sin cambiar el nombre/la matrícula".
- [Baja lógica mal interpretada como borrado → pérdida de turnos] → Mitigación: tests de "desactivar sillón con turnos conserva el turno" y ausencia de endpoints `DELETE`.
- [C-05 apply después → las reglas RBAC cambiarán el acceso a estas rutas] → Mitigación: C-03 no toca seguridad; C-05 aplica la matriz sobre las rutas ya existentes.
- [Datos personales en ejemplos → incumplimiento D1/D2] → Mitigación: solo fixtures ficticias (DNI `0000000X`, teléfono `549110000000X`, "Paciente Uno") y logs con identificadores, nunca DNI/teléfono/nombre completo.

## Migration Plan

Sin migración de datos: H2 es en memoria y no hay esquema nuevo (las entidades y columnas ya las creó C-01). El despliegue es un PR de la rama `feature/C-03-configurar-recursos` a `main` revisado por el líder (E2), con commits *conventional commits* en español y luego comprimidos por squash. Rollback: revertir el PR; no hay datos de producción que recuperar. Después de verify, el líder decide `openspec sync specs` y `openspec archive` siguiendo el estado real del CLI (G1). Próximo paso inmediato: **esperar la revisión del líder (governance MEDIO, G2); no ejecutar apply todavía**. Una vez aprobado, el siguiente comando será `/opsx:apply configurar-recursos`.

## Open Questions

- PA-02: confirmación del Product Owner sobre "una franja por día para todo el consultorio" (SU-03). No bloquea la propuesta porque se adoptó como decisión del change; si la respuesta difiere, se vuelve a `propose`.
