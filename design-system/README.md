# Design System

Mode: system-and-mockups
Approval: approved
Decision source: elección del usuario en la fase `visual-design` de la fundación (`/active-orchestrator`), 2026-11-20; resuelta como `system-and-mockups` en el change C-09 (2026-11-23).

## Por qué está diferido

- El primer change del roadmap ("crear turno sin solapamientos") es dominio + API, sin pantallas (`knowledge-base/09_decisiones_y_supuestos.md`, DD-06).
- La consigna no exige interfaz gráfica, y la entrega es el 2026-11-25.
- La interfaz web (Thymeleaf, DD-05) llega en un change posterior.

## Qué pasa en el primer change visual

`deferred` **no** es un opt-out permanente. Antes de proponer el primer change que agregue o modifique una pantalla, se invoca la skill `design-system` y se pregunta **una sola vez** entre:

- `system-and-mockups`: sistema de diseño + mockup aprobado por cada cambio visual.
- `system-only`: sistema de diseño, sin mockups por cambio.
- `off`: sin sistema ni mockups.

La decisión `system-and-mockups` reemplaza el `deferred` en este archivo y en `.active-orchestrator-state.json` (`visual_design`).

## Fundación (C-09, 2026-11-23)

Con la política `system-and-mockups` aprobada, C-09 creó la fundación mínima reutilizable por C-10:

- `brand.md`: identidad, voz, paleta, tipografía, componentes y accesibilidad.
- `brand.js`: export de la paleta y la forma para herramientas.
- `tokens.css`: variables de color, tipografía, espaciado y forma.
- `components.html`: catálogo de componentes con datos ficticios.
- `assets/logo.svg`: marca tipográfica simple.
- `previews/README.md`: ubicación de capturas (sin Playwright MCP en este apply).
- `openspec/changes/ui-base-y-gestion/mockups/`: mockups HTML de login, panel y las cinco
  pantallas de gestión, aprobados por la política vigente.

Los tokens se replican en `src/main/resources/static/css/estilos.css` para la aplicación Thymeleaf.
Un cambio de token requiere revisión del líder (política `system-and-mockups`).
