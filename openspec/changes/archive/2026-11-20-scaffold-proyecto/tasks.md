# Tasks

## 1. Esqueleto del proyecto

- [x] 1.1 Generar el proyecto con Spring Initializr (Maven, Java 17, Spring Boot 4.2.0 — ver D1, grupo `ar.odonto`, artefacto `turnos-odontologia`, paquete `ar.odonto.turnos`; starters web, validation, data-jpa, security, thymeleaf, actuator, h2) en la raíz del repo, sin pisar archivos existentes. Agregar `spring-security-test` y `thymeleaf-extras-springsecurity6`. Verificar que `mvnw.cmd -v` funciona y anotar la versión de Boot usada.
- [x] 1.2 Crear la estructura de paquetes `domain/{model,port,error,rules}`, `application`, `infrastructure/{persistence,config}` y `web/{api/error,view}` (con `package-info.java` que documente el dueño de cada uno según `CHANGES.md`). Dar bit ejecutable a `mvnw` (`git update-index --chmod=+x mvnw`). Verificar que `mvnw.cmd test` levanta el contexto con el test generado.
- [x] 1.3 Configurar `application.yml` (H2 en memoria, `open-in-view: false`, exponer solo `health` en actuator, propiedades `turnos.*`) y `application-dev.yml` (consola H2, logs SQL). Verificar que la app arranca con `mvnw.cmd spring-boot:run` y con `-Dspring-boot.run.profiles=dev`.

## 2. Configuración transversal (tiempo y parámetros)

- [x] 2.1 Crear `TurnosProperties` (`@ConfigurationProperties("turnos")`, validado, con los valores por defecto 5 min / 6 h / zona AR) y el bean `Clock` en la zona configurada. Verificar con tests: valores por defecto, valor sobrescrito por propiedad (10 min) y `Clock` en `America/Argentina/Buenos_Aires` aunque la JVM esté en UTC (escenarios de la spec "Parámetros de negocio configurables" y "Zona horaria y reloj del sistema").
- [x] 2.2 Crear `support/RelojDePrueba` (`Clock.fixed` en la zona AR) y verificar con un test que el "ahora" de un bean que usa el `Clock` es exactamente el instante fijo (escenario "Reloj fijo para verificación").

## 3. Errores de la API

- [x] 3.1 Crear `CodigoError` con todos los códigos de la spec y su `HttpStatus`, y `ReglaDeNegocioException`. Verificar con un test parametrizado que cada código tiene el estado esperado (409, 404, 400 o 422; `ERROR_INTERNO` 500) y que están todos los códigos listados en la spec.
- [x] 3.2 Implementar `ManejadorGlobalDeErrores` (`ProblemDetail` con propiedad `codigo`; mapeo de validación, JSON ilegible, tipo incorrecto, integridad única y errores inesperados). Verificar con un controlador **solo de test** y MockMvc los escenarios de la spec: violación de regla (409 `CHOQUE_SILLON`, `application/problem+json`), campo obligatorio ausente (400 con el nombre del campo), JSON ilegible (400) y error inesperado (500 `ERROR_INTERNO` sin traza en el cuerpo).

## 4. Modelo de datos y repositorios

- [x] 4.1 Crear las entidades `Consultorio`, `HorarioAtencion`, `Profesional`, `Sillon`, `Prestacion`, `Paciente`, `Usuario` (enum `Rol`) y `Turno` (enum `TurnoEstado`) según `knowledge-base/04` y D2: invariantes en constructores (`HORARIO_INVALIDO`, `DURACION_INVALIDA`), restricciones únicas e índices. Verificar con tests unitarios de invariantes (horario `apertura >= cierre` rechazado; duración 0, negativa o no múltiplo de 5 rechazada).
- [x] 4.2 Crear los puertos en `domain/port` y sus implementaciones Spring Data en `infrastructure/persistence` (D3), incluidas `existsByMatricula`, `existsByNombre`, `existsByDni`, `existsByUsername` y el horario por consultorio y día. Verificar con `@DataJpaTest` que guardar un duplicado de matrícula o DNI viola la restricción única.
- [x] 4.3 Implementar las consultas de choque de `TurnoRepository` (D4) y verificar con `@DataJpaTest`, existente 09:30-10:00 y margen 5: un nuevo turno a las 10:05 del mismo sillón o profesional **no** aparece como choque, uno a las 10:04 **sí**; un turno `CANCELADO` nunca aparece; un turno de otro sillón o profesional no aparece; el paciente se superpone sin margen; `excluirTurnoId` excluye ese turno.
- [x] 4.4 Implementar `buscarActivosDeProfesionalEntre` y verificar que devuelve solo turnos `RESERVADO` del rango, ordenados por `inicio`.
- [x] 4.5 Verificar con `@SpringBootTest` que, sin perfil `dev`, la base arranca vacía (escenario "Arranque sin perfil dev").

## 5. Seguridad provisoria e identidad del solicitante

- [x] 5.1 Crear `Solicitante` (record en `domain/model`), el puerto `UsuarioActual` y el `PasswordEncoder` BCrypt en `infrastructure/config`.
- [x] 5.2 Crear `PermisivaSecurityAutoConfiguration` (D8) registrada en `AutoConfiguration.imports`, con `permitAll`, CSRF deshabilitado y el bean por defecto de `UsuarioActual` (rol `ADMIN`, usuario `sistema`). Verificar con tests: un endpoint de test responde sin credenciales, y declarar en un test un `SecurityFilterChain` propio que exija autenticación desactiva la cadena permisiva (responde 401) sin errores de arranque.
- [x] 5.3 Verificar con MockMvc que `GET /actuator/health` responde 200 con `status: UP` sin credenciales (escenario "Aplicación sana").

## 6. Datos de ejemplo de desarrollo

- [x] 6.1 Crear `DatosDeEjemploDev` (`@Profile("dev")`, idempotente) con el consultorio y su horario L-V 08:00-20:00, 2 sillones, 2 profesionales, 4 prestaciones y 3 pacientes ficticios (DNI `0000000X`, tel `549110000000X`). Verificar con `@SpringBootTest` y perfil `dev` las cantidades y los patrones ficticios (escenarios "Arranque con perfil dev" y "Datos de pacientes ficticios").
- [x] 6.2 Crear los usuarios `admin`, `secretaria` y `ana` solo si están las variables `SEED_*_PASSWORD`, con hash BCrypt. Verificar con tests los dos escenarios: con variables existen los 3 usuarios con hash (no el texto plano); sin variables no hay usuarios y el log no contiene ninguna contraseña.

## 7. Documentación e integración

- [x] 7.1 Reescribir `README.md` conservando la lista de integrantes: requisitos (JDK 17, `JAVA_HOME`), cómo correr la app (`mvnw.cmd spring-boot:run`, perfil `dev`, variables `SEED_*_PASSWORD`), cómo correr los tests, consola H2, nota de `src/test/java` (DD-07), mapa de la documentación (`docs/`, `knowledge-base/`, `CHANGES.md`, `AGENTS.md`) y flujo de ramas y PR. Verificar que cada comando documentado funciona tal como está escrito.
- [x] 7.2 Correr la suite completa con `mvnw.cmd test` desde un clon limpio del branch y verificar que termina en verde sin servicios externos (escenario "Tests desde un clon limpio").

## Workflow follow-up

- Verify: correr `openspec validate scaffold-proyecto` y la suite completa; revisar que no haya datos reales ni secretos en el diff.
- Archive: `openspec-archive-change scaffold-proyecto` (crea `openspec/specs/base-del-sistema/spec.md`) y marcar `[x]` en C-01 de `CHANGES.md`.
- PR `feat(base-del-sistema): scaffold del proyecto` a `main`, squash and merge, y avisar al equipo que se abrió el GATE 0.
