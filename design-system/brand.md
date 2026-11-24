# Marca y sistema visual — Turnos Odontología

- **Modo de política visual**: `system-and-mockups`
- **Estado**: `approved` (2026-11-23, `design-system/README.md` y `.active-orchestrator-state.json`)
- **Alcance**: base de la interfaz de gestión (C-09). C-10 reutiliza estos tokens y componentes.

## Identidad

Producto de gestión para consultorios odontológicos argentinos de 1 a 5 profesionales. La interfaz
transmite **orden, higiene y confianza**: superficies claras, un verde azulado profundo como color de
acción y mucho aire entre bloques. Nada de decoración clínica agresiva ni iconografía de dientes
infantil; la marca es el trabajo bien hecho, no el símbolo.

- **Voz**: profesional, directa, en español rioplatense neutro ("Secretaría", "Turno", "Sillón").
- **Tono visual**: clínico sereno; contraste alto para leer rápido en mostrador.
- **Regla de datos**: todo dato personal de ejemplo es ficticio (D1). DNI `0000000X`, teléfonos
  `549110000000X`, nombres "Paciente Uno", "Profesional Ejemplo".

## Paleta

| Token | Valor | Uso |
|---|---|---|
| `--color-fondo` | `#f5f7f7` | Fondo de aplicación |
| `--color-superficie` | `#ffffff` | Tarjetas, tablas, formularios |
| `--color-tinta` | `#16232b` | Texto principal |
| `--color-tinta-suave` | `#4d5f68` | Texto secundario |
| `--color-primario` | `#0f766e` | Acción principal, enlaces, foco |
| `--color-acento` | `#a15c07` | Avisos y sobreturnos |
| `--color-peligro` | `#b42318` | Errores de regla, cancelar |
| `--color-exito` | `#18794e` | Estados activos / confirmaciones |

Estados de recurso: chip verde `Activo` (`--color-exito-suave`), chip gris `Inactivo`
(`--color-superficie-alt`).

## Tipografía

- Familia de sistema (sin fuentes externas para no depender de la red en el consultorio).
- Títulos en semibold; cuerpo 16 px con interlineado 1.55; tablas y etiquetas 14 px.
- Jerarquía por tamaño y peso, no por color: solo el texto secundario y los estados usan color.

## Componentes base

1. **Barra superior** con marca, navegación por rol y salida de sesión.
2. **Tarjeta de sección** (`panel`): título, descripción y enlace de acción.
3. **Tabla de gestión**: encabezado en mayúsculas suaves, filas separadas por borde, sin rayas
   cebra para mantener calma visual.
4. **Formulario de alta** en tarjeta lateral: campos etiquetados arriba, control de 40 px, botón
   primario a la derecha.
5. **Alerta de regla**: bloque con el `codigo` de `CodigoError` en versalitas y el mensaje del dominio.
6. **Chip de estado** y **chip de rol**.
7. **Botón secundario** (borde) y **enlace de peligro** (cancelar).

## Accesibilidad

- Contraste mínimo AA en texto y controles; foco visible con `--sombra-foco`.
- Navegación con un solo `h1` por pantalla, `label` asociado a cada campo y `aria-live` en las alertas.
- Objetivos táctiles de 40 px; tablas con `scope="col"`.
- Diseño probado a 390 px y 1440 px de ancho.

## Archivos

- `design-system/tokens.css`: variables de color, tipografía, espacio y forma.
- `design-system/components.html`: catálogo navegable de componentes con datos ficticios.
- `design-system/brand.js`: export de la paleta para herramientas.
- `design-system/assets/logo.svg`: marca tipográfica simple (sin claims clínicos).
- `design-system/previews/`: capturas (si hay Playwright MCP); hoy referencia textual.
