# Proposal

## Why

Los cambios C-01 a C-08 construyen el núcleo del sistema (dominio, casos de uso y API REST), pero la ola 1 no agregó pantallas a propósito: no existe layout Thymeleaf, ni vista de `/login`, ni pantalla de gestión. Sin interfaz, los actores del consultorio (secretaría, odontólogo, administrador) no pueden operar el sistema que ya existe, y la entrega prevista para el 2026-11-25 quedaría incompleta como producto. El gate de diseño visual quedó resuelto como `system-and-mockups` (aprobado por el líder) y el primer change con pantallas (C-09) debe arrancar con la base de la interfaz antes de que C-10 pueda reutilizarla.

## What Changes

- Nueva capa de vistas Thymeleaf en `web/view/gestion/` con controladores que consumen los casos de uso de C-03 (profesionales, sillones, prestaciones, horario) y C-04 (pacientes) sin duplicar reglas de negocio.
- Layout base Thymeleaf en `templates/layout/` y recursos estáticos en `static/` (CSS).
- Vista `/login` propia (Thymeleaf) que reemplaza la página por defecto de Spring Security, reutilizando la sesión y el form login definidos en C-05.
- Pantallas de gestión (profesionales, sillones, prestaciones, horario y pacientes) con visibilidad por rol mediante `thymeleaf-extras-springsecurity6`.
- Reactivar CSRF para los formularios Thymeleaf (desactivado en C-05 por no existir vistas) antes del apply.

## Capabilities

### New Capabilities

- `interfaz-gestion`: base de la interfaz web de gestión: layout de Thymeleaf, vista de login y pantallas de gestión con control de visibilidad por rol.

### Modified Capabilities

_(ninguna)_

## Impact

- **Código nuevo**: `web/view/gestion/**`, `templates/**`, `static/**`, `web/view/**` y sus tests propios.
- **Código reutilizado**: casos de uso de C-03 y C-04, `Solicitante` y la cadena de seguridad de C-05, `CodigoError`/`ProblemDetail` para mostrar errores de reglas en las vistas.
- **Seguridad**: reactivar CSRF en el `SecurityFilterChain` de C-05 antes del apply (los forms Thymeleaf requieren token CSRF válido); decisiones registradas en el design de este change.
- **Dependencias**: C-03, C-04, C-05 (GATE 2). Este change bloquea a C-10 (`ui-turnos-y-agenda`).
