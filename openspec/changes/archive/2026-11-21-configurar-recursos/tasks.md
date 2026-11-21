# Tasks

Convención TDD obligatoria: cada tarea numerada sigue **RED** (escribir el test del escenario y verificar que falla) → **GREEN** (implementar lo mínimo hasta que pase) → **TRIANGULATE** (repetir con otro dato ficticio para confirmar la regla) → **REFACTOR** (limpiar sin cambiar el comportamiento). Un test por escenario de `specs/configuracion-consultorio/spec.md`. Los datos de prueba son siempre ficticios (D1: DNI `0000000X`, teléfono `549110000000X`, "Paciente Uno") y los logs solo identificadores (D2). Sin `@Disabled`, sin `-DskipTests` y sin dependencias nuevas (J4, J5). C-03 no tiene UI (`Decision: no (no UI)`).

## 1. Safety net y límites del change

- [x] 1.1 Antes de tocar cualquier archivo, ejecutar `./mvnw test` (Windows: `mvnw.cmd test`) y verificar que la suite existente esté en verde; annotar el resultado como safety net.
- [x] 1.2 Verificar con `git status --short` que la rama `feature/C-03-configurar-recursos` no tiene cambios de código previos y que la planificación vive solo en `openspec/changes/configurar-recursos/`; confirmar que no se editarán archivos compartidos de C-01 (E1).

## 2. Prestaciones (US-001, RN-CF-01/03/04)

- [x] 2.1 Escenario "Alta feliz de Control" (30 min): RED `AltaPrestacionTest#altaDeControl30Minutos` (espera 201, `duracionMinutos` 30, `activa` true y `Location`); GREEN caso de uso `AltaPrestacion` + `POST /api/prestaciones` con DTOs; TRIANGULATE con "Limpieza" 45 min; REFACTOR del mapper de respuesta. Verificación: test en verde y respuesta sin entidades JPA (J2).
- [x] 2.2 Escenario "Duración no múltiplo de 5": RED `AltaPrestacionTest#duracion7Rechazada` (espera 422 y `DURACION_INVALIDA` apoyándose en la invariante del modelo); GREEN dejar propagar `ReglaDeNegocioException` al manejador global; TRIANGULATE con 11 min; REFACTOR sin duplicar la validación. Verificación: test en verde y ninguna prestación persistida.
- [x] 2.3 Escenario "Duración cero": RED `AltaPrestacionTest#duracionCeroRechazada` (espera 422 y `DURACION_INVALIDA` en el límite 0); GREEN confirmar el mismo camino de error; TRIANGULATE con duración negativa; REFACTOR de nombres de test. Verificación: test en verde y sin código de error nuevo.
- [x] 2.4 Escenario "Editar duración y desactivar": RED `EditarPrestacionTest#editarDuracionYDesactivar` (espera 200 con 45 min y `activa` false); GREEN caso de uso `EditarPrestacion` + `PUT /api/prestaciones/{id}` con `@Transactional`; TRIANGULATE con reactivación; REFACTOR del comando de edición. Verificación: test en verde y estado persistido.
- [x] 2.5 Escenario "Editar con duración inválida": RED `EditarPrestacionTest#editarConDuracion7` (espera 422 y duración anterior conservada); GREEN validar antes de guardar; TRIANGULATE con 0; REFACTOR. Verificación: test en verde y sin cambio en base.
- [x] 2.6 Escenario "Prestación inexistente": RED `EditarPrestacionTest#editarInexistente` (espera 404 y `RECURSO_INEXISTENTE`); GREEN resolver con `findById` del puerto compartido; TRIANGULATE con otro id; REFACTOR. Verificación: test en verde y sin excepción 500.
- [x] 2.7 Escenario "Campo obligatorio ausente": RED `PrestacionesApiTest#crearSinNombre` con MockMvc (espera 400, `ENTRADA_INVALIDA` y mención de `nombre`); GREEN Bean Validation en el record de request con `@Valid`; TRIANGULATE con `duracionMinutos` ausente; REFACTOR de mensajes. Verificación: test en verde y sin ejecutar reglas de negocio.

## 3. Sillones (US-002, RN-CF-03/04)

- [x] 3.1 Escenario "Alta de sillón": RED `AltaSillonTest#altaDeSillonUno` (espera 201, `nombre` "Sillon 1", `activo` true); GREEN caso de uso + `POST /api/sillones`; TRIANGULATE con "Sillon 2"; REFACTOR. Verificación: test en verde y `Location` presente.
- [x] 3.2 Escenario "Nombre de sillón duplicado": RED `AltaSillonTest#nombreDuplicado` (espera 409 y `DATO_DUPLICADO` vía `existsByNombre`); GREEN chequear antes de crear; TRIANGULATE con distinta capitalización según normalización elegida; REFACTOR. Verificación: test en verde y un solo sillón guardado.
- [x] 3.3 Escenario "Editar sin cambiar el nombre" (decisión d): RED `EditarSillonTest#editarMismoNombreNoDuplicado` (espera 200 y **no** 409); GREEN comparar el nombre normalizado con el actual y solo llamar a `existsByNombre` si cambió; TRIANGULATE editando solo `activo`; REFACTOR del helper de unicidad. Verificación: test en verde y cero llamadas innecesarias al puerto (fake con contador).
- [x] 3.4 Escenario "Sillón inexistente": RED `EditarSillonTest#editarInexistente` (espera 404 y `RECURSO_INEXISTENTE`); GREEN con `findById`; TRIANGULATE con otro id; REFACTOR. Verificación: test en verde.

## 4. Profesionales (US-003, RN-CF-03/04)

- [x] 4.1 Escenario "Alta de profesional": RED `AltaProfesionalTest#altaConMatriculaUnica` (espera 201, matrícula "M-0001", `activo` true, teléfono ficticio `5491100000001`); GREEN caso de uso + `POST /api/profesionales`; TRIANGULATE con "M-0002"; REFACTOR. Verificación: test en verde y sin dato real (D1).
- [x] 4.2 Escenario "Matrícula duplicada": RED `AltaProfesionalTest#matriculaDuplicada` (espera 409 y `DATO_DUPLICADO` vía `existsByMatricula`); GREEN chequear antes de crear; TRIANGULATE con otra matrícula repetida; REFACTOR. Verificación: test en verde y un solo profesional guardado.
- [x] 4.3 Escenario "Editar sin cambiar la matrícula" (decisión d): RED `EditarProfesionalTest#editarMismaMatriculaNoDuplicada` (espera 200 y no 409); GREEN omitir la consulta de unicidad cuando la matrícula es idéntica; TRIANGULATE editando solo teléfono/activo; REFACTOR. Verificación: test en verde con fake que cuenta llamadas.
- [x] 4.4 Escenario "Matrícula enviada distinta en la edición": RED `EditarProfesionalTest#matriculaInmutable` (espera 400 y `ENTRADA_INVALIDA`, matrícula original conservada); GREEN rechazar antes de cualquier `existsByMatricula`; TRIANGULATE con otro valor; REFACTOR. Verificación: test en verde y entidad sin cambios.
- [x] 4.5 Escenario "Profesional inexistente": RED `EditarProfesionalTest#editarInexistente` (espera 404 y `RECURSO_INEXISTENTE`); GREEN con `findById`; TRIANGULATE con otro id; REFACTOR. Verificación: test en verde.
- [x] 4.6 Escenario "Alta sin usuario asociado" (decisión c): RED `ProfesionalesApiTest#altaNoCreaUsuario` (espera 201 sin `usuario`, `password` ni `usuarioId` en el body); GREEN DTO de respuesta solo con datos del profesional; TRIANGULATE verificando que `UsuarioRepository.count()` no cambia; REFACTOR. Verificación: test en verde y ningún archivo de `domain/model/Usuario` ni `UsuarioRepository` tocado (E1).

## 5. Baja lógica y listado (RN-CF-03)

- [x] 5.1 Escenario "Desactivar sillón con turnos" (borde): RED `DesactivarSillonTest#desactivarConTurnoConservaTurno` (espera 200 con `activo` false y el turno RESERVADO sigue consultable con el mismo sillón); GREEN caso de uso que llama a `desactivar()` sin borrar; TRIANGULATE desactivando profesional y prestación con turnos; REFACTOR. Verificación: test en verde y cero `delete` sobre recursos.
- [x] 5.2 Escenario "Reactivar sillón": RED `DesactivarSillonTest#reactivarSillon` (espera 200 con `activo` true); GREEN usar `activar()` desde el mismo `PUT`; TRIANGULATE reactivar profesional; REFACTOR. Verificación: test en verde.
- [x] 5.3 Escenario "Sin borrado físico": RED `SillonesApiTest#deleteNoDisponible` (espera que `DELETE /api/sillones/{id}` no ejecute borrado y el sillón siga listado); GREEN comprobar que solo existen `GET/POST/PUT`; TRIANGULATE con `DELETE /api/profesionales/{id}`; REFACTOR. Verificación: test en verde y sillón presente tras la petición.
- [x] 5.4 Escenario "Listado con recursos mixtos": RED `ListarSillonesTest#listadoIncluyeInactivos` (espera 200 con sillón activo e inactivo y sus estados); GREEN caso de uso `ListarSillones` + `GET /api/sillones`; TRIANGULATE con profesionales y prestaciones; REFACTOR de la vista de listado. Verificación: test en verde y respuesta sin entidades JPA.
- [x] 5.5 Escenario "Sin recursos configurados": RED `ListarPrestacionesTest#listadoVacio` (espera 200 con `[]` en base vacía); GREEN devolver lista vacía sin error; TRIANGULATE con sillones vacíos; REFACTOR. Verificación: test en verde.

## 6. Lectura del horario (US-004, RN-CF-02, decisión a)

- [x] 6.1 Escenario "Consultorio sin franjas": RED `ConsultarHorarioTest#sieteDiasCerrados` (espera 200 con siete días y `apertura`/`cierre` null); GREEN caso de uso `ConsultarHorario` + `GET /api/consultorio/horario` usando `findAll` del puerto compartido; TRIANGULATE con otra zona del servidor (sin `Clock`); REFACTOR del armado de la vista semanal. Verificación: test en verde y ninguna lectura de hora actual.
- [x] 6.2 Escenario "Día con franja configurada": RED `ConsultarHorarioTest#lunesConFranja` (espera lunes 08:00-20:00 y el resto null); GREEN mapear la entidad existente a DTO; TRIANGULATE con martes 09:00-18:00; REFACTOR. Verificación: test en verde y formato `HH:mm`.

## 7. Reemplazo y cierre del horario (decisiones a y b)

- [x] 7.1 Escenario "Configurar una franja nueva": RED `ReemplazarHorarioTest#configurarLunes` (espera 200 con 08:00-20:00 y persisted); GREEN caso de uso `ReemplazarHorario` + `PUT /api/consultorio/horario` con `ConsultorioRepository.findAll()` y `save`; TRIANGULATE con miércoles; REFACTOR. Verificación: test en verde y GET coherente.
- [x] 7.2 Escenario "Reemplazar franja existente": RED `ReemplazarHorarioTest#reemplazarLunes` (espera 09:00-18:00, una sola franja); GREEN usar `cambiarFranja()` sobre la entidad encontrada por `findByConsultorioIdAndDiaSemana`; TRIANGULATE con otro par de horas; REFACTOR. Verificación: test en verde y `count()` de franjas del día igual a 1.
- [x] 7.3 Escenario "Sin consultorio configurado": RED `ReemplazarHorarioTest#sinConsultorio` (espera 404 y `RECURSO_INEXISTENTE`); GREEN resolver antes de crear la franja; TRIANGULATE con base vacía y GET (que sí debe responder cerrado); REFACTOR. Verificación: test en verde.
- [x] 7.4 Escenario "Apertura igual al cierre" (borde): RED `ReemplazarHorarioTest#aperturaIgualCierre` (espera 422 y `HORARIO_INVALIDO` desde la invariante del modelo); GREEN propagar la excepción al manejador global; TRIANGULATE con otro valor igual; REFACTOR sin duplicar validación. Verificación: test en verde y sin franja guardada.
- [x] 7.5 Escenario "Apertura posterior al cierre": RED `ReemplazarHorarioTest#aperturaPosteriorCierre` (espera 422 y `HORARIO_INVALIDO`); GREEN mismo camino de error; TRIANGULATE con medianoche; REFACTOR. Verificación: test en verde.
- [x] 7.6 Escenario "Cerrar lunes con franja previa" (decisión b): RED `ReemplazarHorarioTest#cerrarDiaConFranja` (espera 200 con null/null y GET sin franja para el lunes); GREEN declarar la interfaz Spring Data propia de C-03 en `application/configuracion/persistencia` con `findByConsultorioIdAndDiaSemana` y `delete`, y usarla solo en el caso de uso transaccional cuando ambos valores son null; TRIANGULATE cerrando otro día y verificando que los demás permanecen; REFACTOR. Verificación: test en verde, sin tocar `domain/port/HorarioAtencionRepository` (E1) y sin asumir un `delete` compartido.
- [x] 7.7 Escenario "Solo un valor de la franja": RED `ReemplazarHorarioTest#soloApertura` (espera 400 y `ENTRADA_INVALIDA`, franja anterior conservada); GREEN validar la combinación null/no-null en el record de request antes del caso de uso; TRIANGULATE con solo `cierre`; REFACTOR. Verificación: test en verde y sin llamadas a `delete`.

## 8. Validación de entrada compartida

- [x] 8.1 Escenario "Día de la semana inválido": RED `HorarioApiTest#diaInvalido` con MockMvc (espera 400 y `ENTRADA_INVALIDA` para "FUNDAY"); GREEN enlace Bean Validation del enum `DayOfWeek` en el record de request; TRIANGULATE con valor vacío; REFACTOR de mensajes. Verificación: test en verde y problema con `codigo` en el body.

## 9. Verificación final

- [x] 9.1 Ejecutar `./mvnw test` (Windows `mvnw.cmd test`) al finalizar y verificar que **toda** la suite está en verde, sin `@Disabled`, sin `-DskipTests` y con un test por cada uno de los 32 escenarios del spec; verificar en la salida que no hay tests omitidos (J4).
- [x] 9.2 Revisar `git diff --name-only` y verificar que los únicos archivos de código tocados están bajo `application/configuracion/**`, `web/api/configuracion/**` y sus tests espejo; confirmar que `pom.xml`, `domain/**`, `infrastructure/**`, `web/api/error/**`, `application.yml`, `README.md` y el seed quedaron intactos (E1, J5).
- [x] 9.3 Verificar con grep que no aparecen `LocalDateTime.now()`, zonas del sistema, anotaciones de seguridad, DNI/teléfono/nombre real en logs ni código de error nuevo (D2, D4, G2); verificar que `openspec status --change configurar-recursos` sigue reportando los cuatro artefactos de planificación presentes.

## Workflow follow-up

- Esperar la revisión del líder antes de apply (governance MEDIO, G2): esta propuesta no habilita implementación por sí sola.
- Una vez aprobado el plan, ejecutar `/opsx:apply configurar-recursos` en una solicitud nueva.
- Después de verify, `openspec sync specs` y `openspec archive` según el estado real que informe el CLI (G1).
