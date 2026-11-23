# Design System

Mode: system-and-mockups
Approval: pending
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

No se creó identidad visual, logo ni tokens provisionales.
