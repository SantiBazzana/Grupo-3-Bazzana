# Design System

Mode: system-and-mockups
Approval: approved (2026-11-23, by user)
Revision: 1
Decision source: elección del usuario en la fase `visual-design` de la fundación (`/active-orchestrator`), 2026-11-20; resuelta como `system-and-mockups` en el change C-09 (2026-11-23).

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

Los tokens se replican en `src/main/resources/static/css/estilos.css` para la aplicación Thymeleaf.
Un cambio de token requiere revisión del líder (política `system-and-mockups`).
