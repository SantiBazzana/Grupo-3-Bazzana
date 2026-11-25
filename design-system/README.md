# Design System

Mode: system-and-mockups
Approval: approved (2026-11-25, by user)
Revision: 2
Decision source: elección del usuario en la fase `visual-design` de la fundación (`/active-orchestrator`), 2026-11-20; resuelta como `system-and-mockups` en el change C-09 (2026-11-23) y actualizada a Revisión 2 el 2026-11-25.

## Estado de la política visual

La política `system-and-mockups` está aprobada y vigente desde C-09 (2026-11-23). Antes de proponer un change que agregue o modifique una pantalla se mantiene el sistema de diseño y se adjunta un mockup aprobado por cada cambio visual.

## Fundación (C-09, 2026-11-23)

Con la política `system-and-mockups` aprobada, C-09 creó la fundación mínima reutilizable por C-10:

- `brand.md`: identidad, voz, paleta, tipografía, componentes y accesibilidad.
- `brand.js`: export de la paleta y la forma para herramientas.
- `tokens.css`: variables de color, tipografía, espaciado y forma.
- `components.html`: catálogo de componentes con datos ficticios.
- `assets/logo.svg`: marca tipográfica simple.
- `previews/README.md`: ubicación de capturas (sin Playwright MCP en este apply).
- `openspec/changes/archive/2026-11-23-ui-base-y-gestion/mockups/`: mockups HTML de login, panel y las cinco
  pantallas de gestión, aprobados por la política vigente.

## Revisión 2 (Rediseño visual profesional, 2026-11-25)

Ajuste visual aprobado por el usuario sobre las plantillas Thymeleaf y hojas de estilo, sin modificar el comportamiento de las specs ni los contratos de backend:

- **Menú lateral de navegación (Sidebar)**: layout clínico moderno con barra lateral izquierda fija (`260px` en escritorio, adaptable en pantallas `<= 820px`), cabecera de marca con isotipo odontológico SVG, enlaces agrupados en *Operación diaria* y *Configuración* con iconografía SVG inline, estado `.activo` automático según la ruta actual y tarjeta inferior de sesión de usuario con avatar, rol y cierre de sesión.
- **Grilla interactiva de botones de horarios en Nuevo Turno (`/turnos/nuevo`)**: selector visual de fecha + grilla de botones de horarios cada 30 minutos (`08:00 hs` a `19:30 hs`, organizada en bloques Mañana y Tarde) sincronizada bidireccionalmente con el campo `#inicio` (`datetime-local`) y conectada en vivo a `GET /api/agenda` para deshabilitar o resaltar en ámbar (cuando se marca *sobreturno*) los horarios ocupados considerando la duración de la prestación y los 5 minutos de margen reglamentario (RN-TU-02 / RN-TU-03 / RN-TU-04).
- **Refinamiento estético general**: paleta clínica teal (`#0f766e`) sobre fondo `#f1f5f9`, tarjetas con bordes `#e2e8f0` y elevación suave, tablas con encabezados estilizados y números tabulares (`tabular-nums`), barra superior contextual y mejoras visuales en Login, Panel, Agenda del día, Detalle de turno y pantallas de Gestión.

Los tokens se replican en `src/main/resources/static/css/estilos.css` para la aplicación Thymeleaf.
Un cambio de token requiere revisión del líder (política `system-and-mockups`).
