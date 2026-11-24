# Tasks

## 1. Fundación visual y política de diseño

- [x] 1.1 Confirmar que los mockups de `openspec/changes/ui-base-y-gestion/mockups/` existen y están aprobados bajo la política `system-and-mockups` antes de crear pantallas; si falta alguno, pausar el change hasta tener aprobación (gate de design).

## 2. Base de vistas Thymeleaf

- [x] 2.1 Crear `templates/layout/` con el layout base y `static/` con los estilos definidos; verificar con test de renderizado de vista que el layout responde.
- [x] 2.2 Crear la vista `/login` (Thymeleaf) que reemplace la de Spring Security con form y token CSRF; verificar con MockMvc: login exitoso redirige al panel (feliz) y POST sin token es rechazado (borde).

## 3. Pantallas de gestión

- [x] 3.1 Crear pantalla de profesionales, sillones, prestaciones y horario que consume los casos de uso de C-03; un test por escenario (feliz, error de negocio, borde).
- [x] 3.2 Crear pantalla de pacientes que consume el caso de uso de C-04; un test por escenario (feliz, error de negocio por DNI duplicado, borde).

## 4. Seguridad en las vistas

- [x] 4.1 Reactivar CSRF en el `SecurityFilterChain` de C-05 para los forms Thymeleaf antes del apply; test de rechazo de POST de vista sin token (borde).
- [x] 4.2 Ajustar la autorización por ruta de las vistas según la matriz RBAC; test de acceso denegado (borde).

## 5. Verificación integral

- [x] 5.1 Ejecutar `./mvnw test` completo en verde sin tests `@Disabled` ni `-DskipTests`, con un test por escenario del spec (feliz, error de negocio, borde).
- [x] 5.2 Ejecutar `openspec validate ui-base-y-gestion --strict` sin errores.

## Workflow follow-up

- Archivar el change con `openspec archive ui-base-y-gestion` una vez revisado y verificado.
- Verificar el resultado archivado.
