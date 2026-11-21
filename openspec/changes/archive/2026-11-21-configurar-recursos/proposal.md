# Proposal

## Why

C-01 y C-02 dejaron en el modelo las invariantes de configuración (duración de prestaciones, franjas horarias, unicidad y baja lógica), pero el sistema todavía no ofrece ninguna forma de cargar o mantener esos datos: hoy solo existen en el seed ficticio del perfil `dev`. Sin alta, edición, listado y desactivación de profesionales, sillones y prestaciones, ni lectura y reemplazo del horario por día, el consultorio no puede arrancar en vacío y configurarse desde la API, y C-09/C-10 no tendrían casos de uso sobre los que construir las pantallas de gestión. Este change cubre US-001 a US-004 y RN-CF-01 a RN-CF-04 como ola 1 del roadmap (C-03, depende solo de C-01).

## What Changes

- Agregar casos de uso en `application/configuracion/` para alta, edición, listado y baja lógica de `Profesional`, `Sillon` y `Prestacion`, y para lectura y reemplazo del horario de atención por día de la semana.
- Exponer los endpoints REST `GET/POST/PUT /api/profesionales`, `GET/POST/PUT /api/sillones`, `GET/POST/PUT /api/prestaciones` y `GET/PUT /api/consultorio/horario`, con DTOs/records de request y response; las entidades JPA nunca se devuelven en la API (J2).
- La baja lógica se hace con `PUT` y `activo=false`: ningún profesional, sillón o prestación se borra físicamente cuando tiene turnos (RN-CF-03); sus turnos existentes se conservan.
- Reutilizar las invariantes ya implementadas en `domain/model` (RN-CF-01 a RN-CF-04) y el catálogo `CodigoError` / `ProblemDetail` de C-01: `DURACION_INVALIDA` (422), `HORARIO_INVALIDO` (422), `DATO_DUPLICADO` (409), `RECURSO_INEXISTENTE` (404) y `ENTRADA_INVALIDA` (400). No se redefinen reglas ni se duplica el seed.
- Adoptar PA-02 según SU-03: una franja por consultorio y día; un día sin franja queda cerrado. `apertura == cierre` o `apertura > cierre` se rechaza con `HORARIO_INVALIDO`. Queda pendiente la confirmación del Product Owner; se registra como decisión asumida por este change, no como cambio de una RN.
- Acotar US-003: el profesional queda vinculable a nivel de modelo, pero la vinculación efectiva con un usuario `ODONTOLOGO` (RN-AU-04) queda para C-05, porque `Usuario` y `UsuarioRepository` son archivos compartidos de C-01 y están fuera del alcance (E1, E5).
- No agregar anotaciones de seguridad: RBAC (ADMIN=CLMB, SECRETARIA y ODONTOLOGO=solo lectura) es responsabilidad de C-05. No inyectar ni usar `Clock`: la configuración no consulta el "ahora" (D4).
- Sin cambios rompientes (**BREAKING**: ninguno). No se modifica `pom.xml`, dependencias, `application.yml`, entidades, puertos compartidos, adaptadores, manejo de errores ni seed.

## Capabilities

### New Capabilities

- `configuracion-consultorio`: alta, edición, listado y baja lógica de los recursos configurables del consultorio (profesionales, sillones y prestaciones) y administración de la franja de atención por día de la semana, con sus códigos de error observables en la API.

### Modified Capabilities

- Ninguna. `base-del-sistema` ya cubre el formato de errores, el catálogo de códigos, el reloj y el seed ficticio; `turnos` ya cubre la creación y consulta de turnos y el uso del horario. Este change agrega una nueva capability sin alterar sus requisitos.

## Impact

- Archivos nuevos permitidos: `src/main/java/ar/odonto/turnos/application/configuracion/**` y `src/main/java/ar/odonto/turnos/web/api/configuracion/**`, más sus tests espejo en `src/test/java/...` (misma estructura de paquetes).
- Se reutilizan sin editar: `domain/model` (`Profesional`, `Sillon`, `Prestacion`, `HorarioAtencion`, `Consultorio`), `domain/port` (`*Repository`), `domain/error` (`CodigoError`, `ReglaDeNegocioException`), `infrastructure/persistence`, `infrastructure/config`, `web/api/error`, `pom.xml`, `application.yml` y `README.md` (regla E1).
- Persistencia: se usa el puerto `HorarioAtencionRepository` disponible (`findByConsultorioIdAndDiaSemana`, `save`, `findAll`); no se asume que exista `delete`/`deleteAll`. Ver `design.md` para la decisión sobre cómo "cerrar" un día con franja previa.
- Seguridad: sin anotaciones ni reglas de rol; C-05 aplicará RBAC después. Datos: solo ficticios en ejemplos y tests (D1: DNI `0000000X`, teléfono `549110000000X`, "Paciente Uno"); nunca se loguean DNI, teléfono ni nombre completo (D2).
- Verificación: `./mvnw test` (`mvnw.cmd test` en Windows) en verde, con un test por escenario del spec y ciclo TDD RED → GREEN → TRIANGULATE → REFACTOR.
- Governance: configuración/datos, nivel MEDIO (G2): se propone y espera revisión del líder antes de pasar a apply. No visual: C-03 no tiene pantallas; C-09 es el primer change visual.
