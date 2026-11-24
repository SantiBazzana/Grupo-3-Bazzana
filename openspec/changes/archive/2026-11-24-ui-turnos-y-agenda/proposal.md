# Proposal

## Why

C-01 a C-08 ya resuelven el núcleo (crear/cancelar turnos, agenda del día y link de WhatsApp) y C-09 entregó el layout, el login y las pantallas de gestión, pero todavía no existe interfaz para operar turnos ni para consultar la agenda. Sin esas pantallas, la secretaría no puede dar/cancelar turnos desde la web y el odontólogo no puede ver su jornada, aunque la API y los casos de uso estén listos. Este change cierra la interfaz del MVP reutilizando la fundación visual ya aprobada.

## What Changes

- Nuevos controladores de vista en `web/view/turnos/` y plantillas en `templates/turnos/` y `templates/agenda/`, con el layout y los tokens de C-09 (revisión 1 de `design-system/`), sin modificar casos de uso ni reglas de negocio.
- Formulario de crear turno: selección de paciente, profesional, sillón, prestación, fecha/hora de inicio y casilla de sobreturno; muestra el `codigo` y el mensaje devueltos por `CrearTurno` (por ejemplo `CHOQUE_SILLON`) en la misma pantalla.
- Listado y detalle de turnos: detalle con datos sin DNI ni teléfono, botón de cancelación que consume `CancelarTurno` y botón de WhatsApp que consume `GenerarLinkWhatsApp`. Si el caso de uso devuelve `PACIENTE_SIN_TELEFONO`, el botón queda oculto/deshabilitado y no se muestra como error de página.
- Vista "Mi agenda" del día: selector de fecha y de profesional para ADMIN/SECRETARIA; para ODONTOLOGO el profesional se fija al vinculado en su `Solicitante` (RN-AG-02, resuelta por `ConsultarAgenda`). La tabla muestra hora, paciente, prestación, sillón y marca de sobreturno, sin DNI ni teléfono.
- Autorización por ruta para las vistas nuevas, alineada con la matriz RBAC: crear/cancelar turno y link de WhatsApp solo ADMIN/SECRETARIA; agenda disponible para los tres roles; odontólogo sin acceso al formulario de creación. Los matchers exactos se proponen como cambio compartido al líder (`infrastructure/security/SeguridadConfig.java`), sin edición directa desde este change.
- Mockups HTML responsivos bajo `openspec/changes/ui-turnos-y-agenda/mockups/`, con datos ficticios y la política `system-and-mockups` (revisión 1). El apply queda bloqueado hasta la aprobación explícita de esos mockups.
- Tests MockMvc con `Clock` fijo y datos ficticios: feliz (crear turno y verlo en la agenda), error de negocio (`CHOQUE_SILLON`), borde (`CANCELACION_TARDIA` y paciente sin teléfono), más CSRF y visibilidad por rol. Sin datos reales, sin credenciales y sin `-DskipTests`.

## Capabilities

### New Capabilities

- `interfaz-turnos`: pantallas web para crear/cancelar turnos, ver el detalle con acciones y consultar la agenda del día según el rol, reutilizando los casos de uso existentes.

### Modified Capabilities

_(ninguna)_

## Impact

- **Código nuevo (propiedad de C-10)**: `src/main/java/ar/odonto/turnos/web/view/turnos/**`, `src/main/resources/templates/turnos/**`, `src/main/resources/templates/agenda/**`, sus tests espejo y los mockups del change. No se crean nuevos casos de uso ni se tocan `domain/**`, `application/**`, `web/api/**`, `pom.xml` ni plantillas de C-09.
- **Código reutilizado**: `CrearTurno`, `ConsultarTurno`, `CancelarTurno`, `ConsultarAgenda`, `GenerarLinkWhatsApp`, los listados de C-03/C-04, `UsuarioActual`/`Solicitante`, `CodigoError` y el manejo de errores de C-01. Las vistas traducen formularios y muestran errores; no duplican reglas.
- **Cambios compartidos solicitados al líder (no incluidos en el apply de C-10)**: (1) matchers RBAC de las nuevas rutas de vista en `SeguridadConfig.java`; (2) enlaces a "Turnos" y "Agenda" en `templates/layout/base.html` y la tarjeta del panel en `templates/gestion/panel.html`, reemplazando el aviso de "próxima entrega". Hasta que el líder los aplique, las pantallas C-10 quedan accesibles por URL y navegables entre sí.
- **Visual**: sistema aprobado `system-and-mockups`, revisión 1. No se cambian tokens ni assets; si un mockup exigiera un token nuevo, se pide revisión de la fundación antes de continuar.
- **Dependencias**: C-09, C-02, C-06, C-07 y C-08 (GATE 3), todas mergeadas en `main`. No se agregan dependencias Maven ni se cambian versiones de Java/Spring Boot (J5).
