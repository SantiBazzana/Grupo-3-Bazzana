# Mockups — ui-turnos-y-agenda (C-10)

- **Política**: `system-and-mockups`, aprobada por el líder el 2026-11-23; ver `design-system/README.md`.
- **Revisión del sistema**: 1 (sin cambios a tokens ni assets en este change).
- **Estado del gate**: `Approval: approved (2026-11-24, by user)`. El apply comenzó después de la aprobación explícita de estos mockups.
- **Datos**: todos ficticios (regla D1). Pacientes "Uno" a "Cuatro", profesionales "Ana Ejemplo" / "Bruno Prueba", sillones 1–3, teléfono de ejemplo `5491100000001`. No se muestran DNI reales ni teléfonos reales.

## Pantallas y estados

| Archivo | Pantalla / estado | Roles de referencia |
|---|---|---|
| `crear-turno.html` | Formulario de nuevo turno con error `CHOQUE_SILLON` visible | ADMIN, SECRETARIA |
| `detalle-turno.html` | Detalle RESERVADO con cancelación y WhatsApp disponible | ADMIN, SECRETARIA |
| `detalle-turno-tardio.html` | Detalle con error `CANCELACION_TARDIA`; el turno sigue RESERVADO | ADMIN, SECRETARIA |
| `detalle-turno-sin-telefono.html` | Detalle con WhatsApp deshabilitado por falta de teléfono (no es error de página) | ADMIN, SECRETARIA |
| `agenda.html` | Agenda del día con filtros, tres turnos activos y marca de sobreturno | ADMIN, SECRETARIA |
| `agenda-vacia.html` | Agenda del día sin turnos RESERVADOS | ADMIN, SECRETARIA |
| `agenda-odontologo.html` | "Mi agenda" con profesional fijo por RN-AG-02 y aviso de `ACCESO_DENEGADO` | ODONTOLOGO |

## Fuentes

- `design-system/tokens.css` (revisión 1): colores, tipografía, espaciado, radios, sombras y foco.
- `design-system/components.html`: referencia de botones, tarjetas, tablas y alertas.
- `openspec/changes/archive/2026-11-23-ui-base-y-gestion/mockups/`: patrón visual y de navegación de C-09.
- `knowledge-base/03_actores_y_roles.md` y `knowledge-base/07_flujos_principales.md`: matriz RBAC y flujos 1–4.
- Specs consolidadas `turnos`, `agenda`, `aviso-whatsapp` y `acceso`: códigos de error y comportamiento observable.

## Previsualización

Abrir cada `.html` directamente en el navegador (no requiere servidor ni dependencias remotas) y revisar a **390×844** y **1440×900**. El CSS local `mockups.css` complementa los tokens compartidos; `brand-name.js` concentra el nombre mostrado en `[data-brand-name]`.

Capturas ya generadas: `previews/<pantalla>-390x844.png` y `previews/<pantalla>-1440x900.png` para las siete pantallas/estados (2026-11-24). Se verificaron marcadores DOM y consola sin errores. El agente no puede inspeccionar píxeles: la aprobación visual la da el usuario sobre estos archivos.

## Verificación de la implementación (C-10 apply)

- `verify/<pantalla>-390x844.png` y `verify/<pantalla>-1440x900.png`: capturas de la app real con perfil `dev` y datos ficticios, para los 7 estados más `acceso-denegado`.
- `verify/reporte.json`: consola sin errores de aplicación, 0 peticiones fallidas y un único 403 esperado (ODONTOLOGO consultando agenda ajena).
- `verify/auditoria-dom.json`: 12 comprobaciones en ambos viewports (título, ancho sin desbordamiento, columnas responsive, tabla con scroll, chip de sobreturno, botón/selector deshabilitados y aviso de RN-AG-02).
- La comparación fina de píxeles contra estos mockups aprobados es manual: ver `Visual verify` en `../design.md`.

Opcionalmente, servir el repo con Node: `npx serve .` desde la raíz y abrir `http://localhost:3000/openspec/changes/ui-turnos-y-agenda/mockups/agenda.html`.

## Reemplazo de valores

- Nombre de marca: editar `NOMBRE_MARCA` en `brand-name.js` (no requiere tocar las siete páginas).
- Colores/tipografía: editar `design-system/tokens.css` y bump de revisión de la fundación (no previsto en C-10).
- Estos mockups son artefactos de revisión: **no se copian** valores de tokens a `mockups.css`.
