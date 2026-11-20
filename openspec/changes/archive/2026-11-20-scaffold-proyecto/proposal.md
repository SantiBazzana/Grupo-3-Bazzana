# Proposal

## Why

El repositorio no tiene código. Los seis changes de la ola 1 (C-02 a C-07 de `CHANGES.md`) necesitan una base común para avanzar en paralelo: el proyecto Spring Boot, el modelo de datos completo, el formato de errores y la configuración de tiempo. Si cada uno armara su parte, todos editarían los mismos archivos (`pom.xml`, entidades, manejador de errores) y los merges chocarían. Este change crea esa base una sola vez y queda como dueño de todos los archivos compartidos.

## What Changes

- Proyecto Java 17 + Spring Boot 4.2 (ver design.md D1) con Maven Wrapper: se clona y se corre sin instalar Maven.
- Modelo de datos completo de `knowledge-base/04_modelo_de_datos.md`: Consultorio, HorarioAtencion, Profesional, Sillon, Prestacion, Paciente, Turno y Usuario, con sus restricciones, índices y repositorios. Incluye las consultas de choque que usarán los changes de la ola 1.
- Catálogo de códigos de error con **todos** los códigos de `knowledge-base/05_reglas_de_negocio.md` y su estado HTTP, más los códigos de validación (entrada inválida, dato duplicado, duración u horario inválidos).
- Formato de error uniforme *Problem Details* (RFC 7807) con el campo `codigo`, para toda la API.
- Reloj del sistema inyectable, fijo en `America/Argentina/Buenos_Aires`, y propiedades de negocio configurables: margen entre turnos (5 min) y anticipación mínima de cancelación (6 h).
- Base H2 en memoria. Con el perfil `dev`: datos de ejemplo **ficticios** y consola H2.
- Health check público (`/actuator/health`).
- Seguridad provisoria **permisiva y reemplazable**, que C-05 sustituye sin editar archivos de este change.
- `README.md` con requisitos, cómo correr la aplicación y los tests, y el flujo de ramas y PR.
- **No** incluye ninguna operación de negocio (crear o cancelar turnos, CRUD, agenda, login): eso pertenece a C-02..C-08.

## Capabilities

### New Capabilities
- `base-del-sistema`: comportamiento transversal que comparten todos los módulos: formato y códigos de error de la API, health check, zona horaria y reloj del sistema, arranque sin infraestructura externa y datos de ejemplo ficticios del entorno de desarrollo.

### Modified Capabilities
<!-- Ninguna: no hay specs previas en openspec/specs/. -->

## Impact

- **Código nuevo**: todo `src/main/java/ar/odonto/turnos/` compartido (`domain/model`, `domain/port`, `domain/error`, `infrastructure/persistence`, `infrastructure/config`, `web/api/error`), `src/main/resources/application*.yml`, `src/test/.../support/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` y `README.md`. Este último reemplaza al README de dos líneas del commit inicial y conserva los integrantes.
- **Dependencias**: Spring Boot starters webmvc, validation, data-jpa, security, thymeleaf, actuator; H2; starters de test por módulo (webmvc, data-jpa, security, validation, actuator, thymeleaf) y thymeleaf-extras-springsecurity6. Después de este change nadie agrega dependencias sin pasar por el líder (regla J5).
- **Equipo**: al mergearse se abre el GATE 0 de `CHANGES.md` y los changes C-02 a C-07 pueden hacer `apply`.
- **Governance**: CRÍTICO. Fija el modelo y los archivos compartidos para los 6 integrantes, y cualquier cambio posterior pasa por el líder (reglas E1 y J5).
