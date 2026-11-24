# Design

## Context

C-01 a C-08 entregaron el núcleo de turnos, cancelación, agenda del día y aviso por WhatsApp; C-09 entregó la base Thymeleaf (layout, login, pantallas de gestión, CSS y política visual `system-and-mockups`, revisión 1 aprobada el 2026-11-23). `openspec/specs/` ya consolida `turnos`, `agenda`, `aviso-whatsapp`, `acceso` e `interfaz-gestion`. No existe todavía interfaz web para crear/cancelar turnos ni para consultar la agenda; C-10 es el último change funcional antes del cierre.

La política visual está resuelta en `design-system/README.md` y `.active-orchestrator-state.json` como `system-and-mockups`, `Approval: approved (2026-11-23, by user)`, `Revision: 1`. Este change reutiliza esa fundación y agrega mockups propios bajo `openspec/changes/ui-turnos-y-agenda/mockups/`.

## Goals / Non-Goals

**Goals:**

- Entregar pantallas Thymeleaf para crear turnos, ver/cancelar un turno, generar el link de WhatsApp y consultar la agenda del día, consumiendo los casos de uso existentes.
- Mostrar errores de negocio con el `codigo` de `CodigoError` en la misma pantalla, sin duplicar reglas.
- Respetar la matriz RBAC y CSRF de C-05/C-09, y la política de no exponer DNI ni teléfono de pacientes en la UI (D1/D2).
- Dejar mockups HTML responsivos aprobables antes del apply, con datos ficticios y revisión 1 del sistema de diseño.

**Non-Goals:**

- Cambiar reglas de dominio, casos de uso, repositorios, entidades, puertos, `pom.xml`, `SeguridadConfig`, `layout/base.html` o plantillas de C-09.
- Agregar nuevas dependencias Maven, ni API REST nueva. La navegación global y los matchers RBAC compartidos se solicitan al líder como follow-up.
- Implementar envío automático de WhatsApp, reprogramación/edición de turnos, historia clínica ni paginación.

## UI Mockups

- `Design system revision: 1` (`design-system/README.md`, `Approval: approved (2026-11-23, by user)`).
- `Decision: yes` (2026-11-24). Change visual en modo `system-and-mockups`; se crean mockups antes del apply. No se usa `provided` ni `no`.
- `Mockups: openspec/changes/ui-turnos-y-agenda/mockups/` (HTML/CSS local, datos ficticios, `brand-name.js` para el nombre mostrado).
- `Approval: approved (2026-11-24, by user)`. El usuario aprobó los mockups de la revisión 1; se habilita el apply.
- `Preview: rendered (2026-11-24, openspec/changes/ui-turnos-y-agenda/mockups/previews/)`. Capturas generadas por navegador en 390x844 y 1440x900 para las 7 pantallas/estados; se verificaron marcadores DOM y consola sin errores. El modelo de este agente no inspecciona píxeles de imagen: el usuario debe revisar las capturas/HTML antes de aprobar.
- `Visual verify: manual (2026-11-24)`. Con navegador Chromium se levantó la app con perfil `dev`, se recorrieron los 7 estados + acceso denegado y se guardaron 16 capturas (390x844 y 1440x900) en `mockups/verify/`. Revisión automática: consola sin errores de aplicación, 0 peticiones fallidas, único 403 esperado (ODONTOLOGO consultando agenda ajena), 12 comprobaciones DOM/layout en ambos viewports sin desbordamiento horizontal, columnas responsive y marcadores de estado presentes (`auditoria-dom.json`, `reporte.json`). Limitación: este modelo no puede inspeccionar píxeles, por lo que la comparación visual fina de las capturas contra los mockups aprobados queda para revisión humana; no se declaró `passed` sin esa inspección.

## Decisions

### D1. Controladores de vista dedicados que consumen casos de uso

**Decisión**: crear `web/view/turnos/TurnoViewController` y `web/view/turnos/AgendaViewController` (o una división equivalente dentro de `web/view/turnos/`) que dependan de `CrearTurno`, `ConsultarTurno`, `CancelarTurno`, `ConsultarAgenda`, `GenerarLinkWhatsApp`, `ListarPacientes`, `ListarProfesionales`, `ListarSillones` y `ListarPrestaciones`. No llaman a `TurnoController`/`AgendaController`.

**Razonamiento**: replica D3 de C-09 y mantiene una sola fuente de verdad de reglas. Los controladores REST ya existen para clientes JSON; las vistas Thymeleaf no deben acoplarse a serialización HTTP.

**Alternativas consideradas:**

- Reusar endpoints REST desde la vista con fetch/cliente HTTP: descartado; agrega complejidad CSRF/AJAX innecesaria y duplica manejo de errores.
- Crear nuevos casos de uso "de vista": descartado; los existentes ya exponen las vistas necesarias.

### D2. Errores de regla en la misma pantalla con `codigo`

**Decisión**: capturar `ReglaDeNegocioException` en el controlador de vista, agregar `errorCodigo` y `errorMensaje` al `Model`, recargar los datos necesarios del formulario/listado y devolver la plantilla correspondiente. Para `PACIENTE_SIN_TELEFONO` en el detalle, el controlador trata el caso como estado de UI (botón deshabilitado/oculto con explicación) y no como alerta de error de página.

**Razonamiento**: es el patrón ya usado por C-09 (`PacienteViewController`, `ProfesionalViewController`) y cumple el requisito de mostrar el código de regla. `PACIENTE_SIN_TELEFONO` es un borde esperado en el detalle, no una falla del formulario.

**Alternativas consideradas:**

- Redirigir con flash y perder el contexto del formulario: descartado para errores de creación; el usuario pierde los valores ingresados.
- Mostrar `PACIENTE_SIN_TELEFONO` como alerta roja: descartado por la spec, que pide acción no disponible sin error de pantalla.

### D3. Agenda respeta RN-AG-02 delegando en `ConsultarAgenda`

**Decisión**: la vista de agenda obtiene el `Solicitante` desde `UsuarioActual`, pasa `fecha` y `profesionalId` elegidos al caso de uso, y renderiza el resultado. Para ODONTOLOGO, el selector de profesional se muestra fijo/deshabilitado con el profesional vinculado; si llega un `profesionalId` ajeno, el caso de uso devuelve `ACCESO_DENEGADO` y la vista lo muestra como acceso denegado.

**Razonamiento**: RN-AG-02 ya está implementada en `ConsultarAgenda`; duplicarla en la vista violaría J1 y puede divergir. La vista solo adapta entrada/salida.

**Alternativas consideradas:**

- Ocultar el selector y forzar el ID en el controlador: útil como UX, pero no suficiente como control; el caso de uso sigue siendo la autoridad.
- Permitir que ODONTOLOGO consulte por query param libre: descartado por RN-AG-02.

### D4. Autorización de rutas compartida se solicita al líder

**Decisión**: C-10 no edita `SeguridadConfig.java`. La propuesta incluye los matchers necesarios para que el líder los aplique en un commit compartido: `/turnos/nuevo` y POST de creación/cancelación solo ADMIN/SECRETARIA; `/agenda/**` y detalle de turno accesibles según matriz (detalle con acciones sensibles protegidas en controlador y plantilla). La navegación en `layout/base.html` y `panel.html` también se solicita al líder.

**Razonamiento**: `SeguridadConfig` es archivo compartido (E1, anti-colisión C-01/C-05). Sin estos matchers, las rutas quedan cubiertas por `anyRequest().authenticated()` y la plantilla oculta acciones por rol, pero la defensa en profundidad de ruta no queda declarada; el apply debe coordinar el cambio compartido.

**Alternativas consideradas:**

- Editar `SeguridadConfig` desde C-10: descartado por E1.
- Agregar una cadena de seguridad propia en C-10: descartado; colisiona con el bean de C-05 y complica el orden.

### D5. Mockups locales, datos ficticios, sin cambio de fundación

**Decisión**: los mockups viven en `mockups/` del change, importan `../../../../design-system/tokens.css`, usan `brand-name.js` local para el nombre mostrado y `mockups.css` solo para layout/composición del mockup. No se copian valores de tokens. Las capturas 390x844 y 1440x900 se guardan en `mockups/previews/`.

**Razonamiento**: cumple la política `system-and-mockups`, la regla D1 de datos ficticios y la restricción de no modificar la fundación aprobada. Un nombre de marca local evita tocar `design-system/brand.js` (revisión 1 aprobada).

## Risks / Trade-offs

- **[Matchers RBAC compartidos pendientes]** → Hasta que el líder aplique el cambio en `SeguridadConfig`, las rutas nuevas dependen de `authenticated()` y de controles en plantilla/controlador. Mitigación: tarea explícita de coordinación y test de seguridad que documente la expectativa; no reclamar defensa de ruta completa hasta el merge compartido.
- **[Navegación global no editable]** → `layout/base.html` y `panel.html` son de C-09. Mitigación: las pantallas C-10 son navegables por URL directa y por enlaces internos entre turnos/agenda; el líder agrega los enlaces globales.
- **[Inspección visual automatizada limitada]** → El agente generó capturas y verificó DOM/consola, pero no puede inspeccionar píxeles. Mitigación: `Preview: rendered` incluye la limitación; la aprobación explícita del usuario sigue siendo obligatoria.
- **[Cancelación desde vista sin endpoint HTML propio]** → Cancelar usa POST con CSRF; hay que evitar GET mutante. Mitigación: formulario POST en el detalle, con redirección PRG.

## Migration Plan

Sin migraciones de base de datos. Cambios aditivos: nuevos controladores Thymeleaf, plantillas y tests en paquetes propios de C-10. Coordinación previa al merge final: líder aplica matchers/navegación compartidos. Rollback = revertir el commit/PR del change.

## Open Questions

- Confirmar con el líder el momento exacto para aplicar los matchers de `SeguridadConfig` y los enlaces de `layout/base.html`/`panel.html` (antes del PR de C-10 o en el mismo PR coordinado).
- Confirmar si el detalle de turno debe ser una ruta propia (`/turnos/{id}`) además del acceso desde agenda; la propuesta lo incluye porque el spec pide detalle con acciones.
