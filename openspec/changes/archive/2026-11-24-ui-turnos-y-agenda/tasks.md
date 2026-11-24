# Tasks

## 1. Gate de diseño (bloqueante)

- [x] 1.1 Verificar que `design-system/README.md` siga en `system-and-mockups`, revisión 1 aprobada, y que los mockups de `openspec/changes/archive/2026-11-24-ui-turnos-y-agenda/mockups/` (7 pantallas/estados) estén listos.
- [x] 1.2 Presentar al usuario las capturas `mockups/previews/*-390x844.png` y `*-1440x900.png` junto con los HTML clicables y obtener aprobación explícita; registrar `Approval: approved (<fecha>, by user)` en `design.md`. Sin este paso NO iniciar el apply.
- [x] 1.3 Confirmar con el líder los matchers RBAC compartidos de `SeguridadConfig.java` y los enlaces en `layout/base.html` / `templates/gestion/panel.html`. No editarlos desde C-10.

## 2. Controladores de vista

- [x] 2.1 Crear `web/view/turnos/` con controladores que consuman `CrearTurno`, `ConsultarTurno`, `CancelarTurno`, `GenerarLinkWhatsApp`, `ConsultarAgenda` y los listados de C-03/C-04. Inyección por constructor (J3), sin lógica de negocio (J1), sin entidades JPA (J2).
- [x] 2.2 GET/POST de `/turnos/nuevo`: cargar opciones, delegar la creación y redirigir a la agenda del profesional/fecha al crear; en error, volver a renderizar el formulario con `errorCodigo`/`errorMensaje`.
- [x] 2.3 GET de `/turnos/{id}`: mostrar detalle sin DNI ni teléfono. POST de cancelación: delegar en `CancelarTurno`, capturar `CANCELACION_TARDIA`/`TURNO_YA_CANCELADO` y mostrar el código en la misma vista (PRG cuando corresponda).
- [x] 2.4 GET de `/agenda`: obtener `Solicitante` desde `UsuarioActual`, pasar fecha y profesional a `ConsultarAgenda`; para ODONTOLOGO fijar/validar su profesional (RN-AG-02). Manejar `ACCESO_DENEGADO` como denegación.
- [x] 2.5 En el detalle, llamar a `GenerarLinkWhatsApp` y tratar `PACIENTE_SIN_TELEFONO` como estado de UI (botón deshabilitado/oculto + explicación), nunca como error de página.

## 3. Plantillas Thymeleaf

- [x] 3.1 Crear `templates/turnos/nuevo.html` reutilizando `layout/base :: layout` y los estilos de `static/css/estilos.css`: selects de paciente/profesional/sillón/prestación, `datetime-local`, checkbox de sobreturno, token CSRF y bloque de error con `errorCodigo`.
- [x] 3.2 Crear `templates/turnos/detalle.html` con datos sin DNI ni teléfono, formulario POST de cancelación con CSRF y enlace a WhatsApp condicionado por disponibilidad del link. Botones sensibles ocultos para ODONTOLOGO con `sec:authorize`.
- [x] 3.3 Crear `templates/agenda/dia.html` con filtros de fecha/profesional, tabla ordenada por inicio con hora, paciente, prestación, sillón y chip de sobreturno; estado vacío claro; sin DNI ni teléfono. Para ODONTOLOGO, selector de profesional deshabilitado y aviso de RN-AG-02.
- [x] 3.4 Comparar cada pantalla implementada con el mockup aprobado correspondiente en los dos viewports; no cambiar el mockup aprobado sin volver a pedir aprobación. Verificación automatizada de marcadores/layout en `mockups/verify/auditoria-dom.json`; comparación fina de píxeles queda registrada como manual en `design.md`.

## 4. Tests MockMvc (un test por escenario del spec)

- [x] 4.1 Base de tests de vistas de turnos con `Clock` fijo, zona `America/Argentina/Buenos_Aires` y datos ficticios (DNI `0000000X`, teléfonos `549110000000X`); nunca `LocalDateTime.now()` (D4).
- [x] 4.2 Crear turno: feliz (queda RESERVADO y aparece en agenda), error de negocio (`CHOQUE_SILLON` visible y sin turno nuevo), borde (sobreturno en otro sillón creado con marca).
- [x] 4.3 Detalle/cancelación: feliz (cancelación con anticipación, desaparece de agenda), error (`CANCELACION_TARDIA`, turno sigue RESERVADO), borde (`TURNO_YA_CANCELADO`, no cambia la fecha original).
- [x] 4.4 WhatsApp: feliz (URL `https://wa.me/` con mensaje codificado), borde (`PACIENTE_SIN_TELEFONO` deshabilita la acción sin alerta de error).
- [x] 4.5 Agenda: feliz (solo activos ordenados, sin DNI/teléfono), error (`ACCESO_DENEGADO` a profesional ajeno), borde (día sin turnos con estado vacío).
- [x] 4.6 Seguridad: POST de vista sin token CSRF rechazado; ODONTOLOGO no accede a crear/cancelar/link; SECRETARIA y ADMIN sí; agenda disponible para los tres roles.

## 5. Verificación integral

- [x] 5.1 Ejecutar `mvnw.cmd test` completo en verde, sin `@Disabled` ni `-DskipTests`, con al menos un test por escenario del spec.
- [x] 5.2 Ejecutar `openspec validate ui-turnos-y-agenda --strict` sin errores.
- [x] 5.3 Con navegador: levantar la app, comparar las 7 pantallas/estados con los mockups aprobados a 390x844 y 1440x900, revisar consola y peticiones fallidas, guardar capturas en `mockups/verify/`, apagar el servidor. Registrado `Visual verify: manual (2026-11-24)` en `design.md` porque la inspección de píxeles no pudo automatizarse con este modelo.
- [x] 5.4 Actualizar `CHANGES.md` (marcar C-10 como completado) y archivar el change con `openspec archive ui-turnos-y-agenda` tras la revisión. El PR a `main` (sin commitear directo, E2; conventional commits en español e identidad de Joaquin, E4) lo ejecuta Joaquin manualmente: no se hicieron commit, push ni PR en este paso.
