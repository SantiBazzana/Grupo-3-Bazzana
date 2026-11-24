# Mockups — ui-base-y-gestion (C-09)

- **Política**: `system-and-mockups` (aprobada por el líder, 2026-11-23; ver `design-system/README.md`).
- **Estado del gate de diseño**: habilitado. La fundación visual vive en `design-system/` y estos
  mockups son la referencia de fidelidad del apply.
- **Datos**: todos ficticios (D1). DNI `0000000X`, teléfonos `549110000000X`, nombres "Paciente Uno",
  "Profesional Ejemplo".

## Pantallas

| Archivo | Pantalla | Roles de referencia |
|---|---|---|
| `login.html` | Vista `/login` con form y token CSRF | público |
| `panel.html` | Panel `/panel` con accesos por rol | los tres roles |
| `pacientes.html` | Gestión de pacientes (alta + búsqueda + error de DNI duplicado) | ADMIN, SECRETARIA |
| `profesionales.html` | Lectura para todos; alta solo ADMIN | ADMIN (alta), resto lectura |
| `sillones.html` | Lectura para todos; alta solo ADMIN; error de nombre duplicado | ADMIN (alta), resto lectura |
| `prestaciones.html` | Duración en múltiplos de 5; error `DURACION_INVALIDA` | ADMIN (alta), resto lectura |
| `horario.html` | Franja por día; error `HORARIO_INVALIDO`; día cerrado | ADMIN (edición), resto lectura |

`mockups.css` replica los tokens de `design-system/tokens.css` para que cada HTML abra sin dependencias.

## Cómo revisarlos

Abrir cualquier `.html` en el navegador. No requieren servidor ni build. Para compararlos con la app,
levantar con `./mvnw spring-boot:run` (perfil `dev`) y recorrer `/login`, `/panel` y `/gestion/**`.

## Correspondencia con las plantillas

`templates/layout/base.html` implementa la barra, el contenedor y el pie; `templates/gestion/*.html`
implementa cada pantalla con los mismos bloques (tarjeta de listado + tarjeta de alta + alerta de
regla). La visibilidad por rol se resuelve con `thymeleaf-extras-springsecurity6`, por eso los
mockups muestran el formulario de alta y una nota de la vista de solo lectura.
