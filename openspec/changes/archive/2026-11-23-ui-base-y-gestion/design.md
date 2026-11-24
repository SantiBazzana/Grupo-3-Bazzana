# Design

Approval: approved (2026-11-23, by user)
Revision: 1

## Context

C-01 dejó una seguridad permisiva reemplazable y C-05 reemplazó la cadena de seguridad por una propia (`SeguridadConfig`) con CSRF desactivado y con nota de que el primer change con vistas (C-09) reactivaría el CSRF para los forms Thymeleaf. Los casos de uso de C-03 (profesionales, sillones, prestaciones, horario) y C-04 (pacientes) ya existen en `application/`, junto con la entidad y el puerto necesarios. Todavía no existen `templates/` ni `static/` ni vistas Thymeleaf. La política visual fue resuelta como `system-and-mockups` y aprobada por el líder (2026-11-23); los mockups del change se ubican en `openspec/changes/archive/2026-11-23-ui-base-y-gestion/mockups/`.

## Goals / Non-Goals

**Goals:**

- Proveer la base de la interfaz Thymeleaf (layout, estáticos, login, pantallas de gestión) que consuma los casos de uso existentes sin duplicar reglas de negocio.
- Reactivar CSRF para los formularios Thymeleaf de login y gestión.
- Aplicar la visibilidad por rol en las pantallas de gestión.

**Non-Goals:**

- Construir el design system desde cero: se reusa el aprobado como `system-and-mockups`.
- Las pantallas de turnos y agenda (C-10): este change entrega solo la base de gestión; C-10 reutiliza layout y fundación.

## Decisions

### D1. Política visual `system-and-mockups` aprobada

**Decisión**: el diseño de las pantallas se valida contra los mockups del change ubicados en `openspec/changes/archive/2026-11-23-ui-base-y-gestion/mockups/`, en línea con la política `system-and-mockups` aprobada por el líder antes del apply.

**Razonamiento**: la fundación `design-system/` fue preparada en el change previo (C-09 en su rama) y la aprobación habilitó la creación de mockups por change.

**Alternativas consideradas:**

- `system-only`: descartada porque la aprobación del líder fue explícita para `system-and-mockups`.
- `off`: descartada por el gate del primer change visual (C-09).

### D2. Reactivar CSRF en la cadena de seguridad de C-05

**Decisión**: en el `SecurityFilterChain` de C-05 se retira `.csrf(AbstractHttpConfigurer::disable)` y se habilita CSRF para los formularios Thymeleaf, eximiendo solo la API REST stateless (`csrf.ignoringRequestMatchers("/api/**", "/h2-console/**")`).

**Razonamiento**: C-05 desactivó CSRF con nota de que C-09 lo reactivaría para las vistas. Con Thymeleaf + `thymeleaf-extras-springsecurity6`, el procesador de CSRF agrega el token a los forms con `th:action`; los formularios de login y gestión quedan protegidos. La exención de `/api/**` se agrega en el apply porque las vistas consumen los casos de uso directamente (D3) y no usan fetch: la API es consumida por clientes JSON que no envían `_csrf` y sus tests previos (C-02 a C-08) no deben modificarse desde C-09 (anti-colisión). Sin la exención, la suite completa de la API pasaba de 314 tests verdes a 130 fallos 403. Los errores de seguridad respetan la misma separación: `/api/**` sin sesión devuelve 401 `application/problem+json` con el campo `codigo`, mientras que las vistas (y cualquier ruta que no sea de la API) redirigen a `/login` conservando el pedido original; `/` redirige a `/panel` cuando hay sesión.

**Alternativas consideradas:**

- Mantener CSRF desactivado: descartada por la directiva de seguridad recibida para este change.
- Desactivar CSRF solo en `/api/**` (originalmente descartada suponiendo que las vistas usarían fetch a la API): se adopta tras el apply porque D3 fija que las vistas consumen los casos de uso y no los endpoints REST; así CSRF queda obligatorio en toda la interfaz.

### D3. Las vistas de gestión consumen los casos de uso directamente

**Decisión**: los controladores de vista (`web/view/gestion/`) dependen de los casos de uso de la capa de aplicación (`application/**`) en vez de llamar a los `@RestController` de la API.

**Razonamiento**: mantiene una única fuente de verdad de las reglas (el dominio) y evita que la vista conozca detalles de serialización REST.

### D4. Errores de regla en la vista

**Decisión**: las pantallas muestran el error de negocio a partir del `codigo` del error (problema con `codigo` existente, reusado vía `CodigoError`/`ProblemDetail`), sin agregar códigos ni mensajes nuevos.

### D5. Visibilidad y autorización por rol de las pantallas

**Decisión**: la autorización por ruta sigue la matriz de `knowledge-base/03_actores_y_roles.md`: `/gestion/pacientes/**` solo ADMIN y SECRETARIA; el resto de `/gestion/**` se lee con los tres roles, pero solo ADMIN hace POST (alta/edición). En la vista, `thymeleaf-extras-springsecurity6` oculta el formulario de alta y los botones de edición a SECRETARIA y ODONTOLOGO. La navegación muestra "Configuración" a los tres roles en modo lectura.

**Razonamiento**: la matriz KB otorga lectura (`L`) de profesionales, sillones, prestaciones y horario a SECRETARIA y ODONTOLOGO, y alta/edición (`C`/`M`) solo a ADMIN; la nota de `CHANGES.md` que decía "la secretaría no ve configuración" se interpreta como "no ve acciones de configuración", porque ocultar la lectura contradiría la matriz RBAC (fuente de verdad del dominio).

## Risks / Trade-offs

- **[Reactivar CSRF]** → Riesgo de que los tests de vistas no envíen token y fallen: mitigación de incluir token en los tests con `spring-security-test` o el token de la vista.
- **[Política visual]** → Si los mockups no quedan aprobados antes del apply, se pausa el cambio: mitigación de corte de alcance (governance MEDIO).
- **[C-10 duplicación]** → Riesgo de que las pantallas de gestión se dupliquen en C-10: mitigación de usar el layout como fundación compartida.

## Migration Plan

Sin migraciones de base de datos. Cambios aditivos: nuevos paquetes de vistas, plantillas y estáticos; un ajuste de seguridad (CSRF) en la cadena existente de C-05; rollback = revert de la rama.

## Open Questions

- Disponibilidad de Playwright MCP para capturas visuales en `design-system/previews/` (390x844 y 1440x900): se responde en el apply; no bloquea el diseño.
