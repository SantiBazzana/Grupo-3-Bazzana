# Tasks

## 1. Infraestructura de seguridad (`infrastructure/security/`)

- [x] 1.1 Crear `SecurityFilterChain` propio en `infrastructure/security/` que reemplace al permisivo de C-01 vía `@ConditionalOnMissingBean`, con rutas públicas (`/login`, `POST /api/auth/login`, `/css/**`, `/js/**`, `/img/**`, `/actuator/health`, `/h2-console` solo perfil `dev`) y todo lo demás requiriendo autenticación; verificar que el contexto de Spring arranca y que `/actuator/health` responde 200 sin sesión (`SecurityFilterChainTest` o `@SpringBootTest` con `MockMvc`).
- [x] 1.2 Crear `UserDetailsServiceImpl` en `infrastructure/security/` que cargue `Usuario` por `username` desde `UsuarioRepository` y valide la contraseña con `PasswordEncoder` (BCrypt) de `PasswordConfig`; verificar con `UserDetailsServiceImplTest` (usuario existente con contraseña correcta carga, contraseña incorrecta lanza excepción, usuario inexistente lanza `UsernameNotFoundException`).
- [x] 1.3 Crear `UsuarioActualImpl` en `infrastructure/security/` que devuelva `Solicitante` con `usuario`, `rol` y `profesionalId` (solo `ODONTOLOGO`) del usuario autenticado en la sesión; verificar con `UsuarioActualImplTest` (mapea `Usuario` → `Solicitante` con rol y profesionalId correctos, y `SISTEMA` no se usa cuando hay sesión).
- [x] 1.4 Rechazar autenticación de `ODONTOLOGO` sin `Profesional` vinculado (RN-AU-04) en el flujo de login o en `UsuarioActualImpl`; verificar con `OdontologoSinProfesionalTest` (401/403 al autenticar un ODONTOLOGO sin profesionalId).

## 2. Caso de uso de autenticación (`application/acceso/`)

- [x] 2.1 Crear caso de uso `AutenticarUsuario` (o similar) en `application/acceso/` que valide credenciales contra `UsuarioRepository` + `PasswordEncoder` y devuelva la identidad del solicitante; verificar con `AutenticarUsuarioTest` (caso feliz: credenciales correctas devuelven `Solicitante`; contraseña incorrecta → error `NO_AUTENTICADO`; usuario inexistente → error `NO_AUTENTICADO`).

## 3. Endpoint de login (`web/api/auth/`)

- [x] 3.1 Crear `AuthController` en `web/api/auth/` con `POST /api/auth/login` que reciba usuario y contraseña, delegue en el caso de uso de autenticación y devuelva 200 en éxito; verificar con `AuthControllerTest` (`@WebMvcTest` o `@SpringBootTest` + MockMvc): 200 con credenciales válidas.
- [x] 3.2 Verificar que credenciales inválidas devuelven 401 con `codigo` `NO_AUTENTICADO` y que el acceso sin permiso devuelve 403 con `codigo` `ACCESO_DENEGADO`; verificar con `AuthControllerTest` (401 para contraseña incorrecta, 403 para ruta sin rol permitido tras login).

## 4. Matriz RBAC por ruta

- [x] 4.1 Configurar en el `SecurityFilterChain` la autorización por ruta según la matriz RBAC de `knowledge-base/03_actores_y_roles.md` (ADMIN: todo; SECRETARIA: pacientes, turnos crear/cancelar, lectura de profesionales/sillones/prestaciones/horario, link WhatsApp; ODONTOLOGO: solo su agenda y lectura de sus recursos); verificar con `RbacSecurityTest` (`@WithMockUser` para cada rol): rutas permitidas responden 200/201, rutas no permitidas responden 403 `ACCESO_DENEGADO`.

## 5. Pruebas de escenarios (un test por escenario)

- [x] 5.1 Test escenario feliz: login correcto de `secretaria` y acceso a una ruta permitida (200); verificar con `spring-security-test` (`formLogin` o `@WithMockUser`).
- [x] 5.2 Test escenario de error de negocio: contraseña incorrecta → 401 `NO_AUTENTICADO`; `ODONTOLOGO` pidiendo una ruta de `ADMIN` → 403 `ACCESO_DENEGADO`; verificar con `AuthSecurityTest` y `RbacSecurityTest`.
- [x] 5.3 Test escenario de borde: acceso sin autenticar a una ruta protegida (401); ruta pública accesible sin login (200); contraseña nunca en logs ni en texto plano (captura de logs con `ListAppender` verificando ausencia de contraseña/DNI/teléfono/nombre); verificar con `AuthSecurityTest` y `RN-DP-03` test de logs.

## 6. Verificación integral y reglas anti-colisión

- [x] 6.1 Ejecutar `./mvnw test` completo en verde sin tests `@Disabled` ni `-DskipTests` (regla J4) y `openspec validate autenticacion-y-roles --strict` sin errores.
- [x] 6.2 Verificar con `git status` y `git diff --name-only origin/main` que ningún archivo compartido de C-01 ni de otros changes fue modificado (reglas anti-colisión 1 y 6 de `CHANGES.md`); solo rutas de C-05 (`infrastructure/security/**`, `application/acceso/**`, `web/api/auth/**` y tests espejo) y `openspec/changes/autenticacion-y-roles/`.
- [x] 6.3 Confirmar que no se agregó ningún código de error nuevo en `CodigoError` (se reutilizan `NO_AUTENTICADO` y `ACCESO_DENEGADO` existentes).

## Workflow follow-up

- [x] Archivar el change con `openspec-archive-change` (`openspec archive autenticacion-y-roles`) una vez revisado y verificado.
- [x] Marcar `[x]` en `CHANGES.md` §C-05.
- [x] Abrir PR desde `feat/autenticacion-y-roles` hacia `main` con título `feat(acceso): autenticacion y roles por ruta` (commits convencionales en español).
