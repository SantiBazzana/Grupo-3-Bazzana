# Grupo-3-Bazzana: Turnos Odontología

Agenda de turnos para consultorios odontológicos argentinos de 1 a 5 profesionales. El sistema **rechaza los choques por profesional y por sillón a la vez**, con margen de 5 minutos entre turnos, sobreturno controlado y cancelación con 6 horas de anticipación. Trabajo Global de Desarrollo de Software, construido con Spec-Driven Development (OpenSpec) y Active Stack.

**Integrantes (Grupo 3):** Bazzana Santiago (líder), De Giuseppe Joaquín, Romero Franco, Castillo Franco, Rocamora Adriano, Guiretti Valentino.

## Requisitos

- **JDK 17** (por ejemplo Eclipse Temurin u Oracle). Verificar con `java -version`.
- No hace falta instalar Maven ni una base de datos: el repo trae el **Maven Wrapper** (`mvnw` / `mvnw.cmd`), y la base es **H2 en memoria**.
- Recomendado: definir `JAVA_HOME`. En Windows PowerShell, una sola vez:

  ```powershell
  [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "User")
  ```

## Correr los tests

```powershell
.\mvnw.cmd test
```

En Linux, macOS o Git Bash: `./mvnw test`. La primera vez el wrapper descarga Maven y las dependencias, así que tarda unos minutos.

Los tests están en `src/test/java` (convención de Maven, decisión DD-07). Cada escenario de las specs de `openspec/specs/` tiene al menos un test automático.

## Correr la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

Levanta en `http://localhost:8080`, con la base vacía. Para verificar que está viva:

```powershell
curl.exe http://localhost:8080/actuator/health
```

### Perfil de desarrollo (datos de ejemplo ficticios)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Con el perfil `dev`:
- **Datos de ejemplo, todos ficticios**: Consultorio Demo (lunes a viernes de 08:00 a 20:00), Sillón 1 y 2, los profesionales Ana Ejemplo y Bruno Prueba, las prestaciones Control (30 min), Limpieza (45), Conducto (60) y Extracción (40), y 3 pacientes con DNI `0000000X`.
- **Consola H2** en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:turnos`, usuario `sa`, sin contraseña).
- **Usuarios de ejemplo** (`admin`, `secretaria` y `ana`): **solo** se crean si definís sus contraseñas por variable de entorno, porque ninguna contraseña se versiona. En la misma terminal, antes de arrancar:

  ```powershell
  $env:SEED_ADMIN_PASSWORD="elegí-una"; $env:SEED_SECRETARIA_PASSWORD="elegí-otra"; $env:SEED_ODONTOLOGO_PASSWORD="y-otra"
  ```

  Las variables valen solo para esa terminal: si abrís otra, hay que definirlas de nuevo.

## Usar la aplicación

### Desde el navegador

Entrá a `http://localhost:8080` (te redirige a `/login`) con alguno de los usuarios de ejemplo y la contraseña que definiste.

| Usuario | Rol | Qué puede hacer |
|---|---|---|
| `admin` | ADMIN | Todo, incluida la configuración (profesionales, sillones, prestaciones, horario) |
| `secretaria` | SECRETARIA | Pacientes, crear y cancelar turnos, agenda de cualquier profesional, link de WhatsApp |
| `ana` | ODONTOLOGO | Solo su agenda (está vinculada a la profesional Ana Ejemplo) y la consulta de la configuración |

Pantallas: **Panel**, **Agenda** (`/agenda`), **Nuevo turno** (`/turnos/nuevo`), el detalle de cada turno con la cancelación y el botón de WhatsApp, y la gestión de **Pacientes**, **Profesionales**, **Sillones**, **Prestaciones** y **Horario**. Si una regla rechaza la operación, la pantalla muestra el código (por ejemplo `CHOQUE_SILLON`) sin perder lo cargado.

### Desde la API

La API usa la misma sesión que las vistas. Primero hay que loguearse y guardar la cookie; después se manda esa cookie en cada pedido (en PowerShell, `curl.exe`):

```powershell
'{"username":"secretaria","password":"la-que-definiste"}' | Out-File -Encoding ascii login.json
```
```powershell
curl.exe -c sesion.txt -H "Content-Type: application/json" -X POST http://localhost:8080/api/auth/login --data "@login.json"
```
```powershell
curl.exe -b sesion.txt "http://localhost:8080/api/agenda?profesionalId=1&fecha=2026-11-26"
```

Sin sesión la API responde `401` con `codigo` `NO_AUTENTICADO`, y con un rol sin permiso responde `403` con `ACCESO_DENEGADO`. Borrá `login.json` al terminar.

### Parámetros de negocio

Se pueden cambiar por variable de entorno sin tocar el código:

| Variable | Default | Regla |
|---|---|---|
| `TURNOS_MARGEN_MINUTOS` | `5` | Margen mínimo entre turnos del mismo profesional o sillón (RN-TU-02/03) |
| `TURNOS_CANCELACION_HORAS_MINIMAS` | `6` | Anticipación mínima para cancelar (RN-CA-01) |
| `TURNOS_ZONA_HORARIA` | `America/Argentina/Buenos_Aires` | Zona de todas las fechas de negocio (RN-GL-01) |

## Stack

Java 17, **Spring Boot 4.2** (WebMVC, Validation, Data JPA, Security, Thymeleaf, Actuator), H2 y JUnit 5 + AssertJ. La API devuelve los errores como *Problem Details* (RFC 7807) con el campo `codigo` (por ejemplo `CHOQUE_SILLON` → 409). El detalle está en [`knowledge-base/02_descripcion_general.md`](knowledge-base/02_descripcion_general.md).

La seguridad usa sesión de Spring Security con contraseñas en BCrypt (C-05). Los formularios de las vistas llevan token CSRF.

## Specs vigentes

Las reglas que el sistema cumple están en [`openspec/specs/`](openspec/specs/), una carpeta por capability:

| Capability | Qué cubre | Change |
|---|---|---|
| `base-del-sistema` | Esqueleto, reloj inyectable, formato de errores | C-01 |
| `turnos` | Crear turno sin choques (profesional, sillón, paciente, margen, sobreturno) y cancelar | C-02, C-08 |
| `configuracion-consultorio` | Profesionales, sillones, prestaciones y horario | C-03 |
| `pacientes` | Alta, búsqueda y edición de pacientes | C-04 |
| `acceso` | Login y permisos por rol | C-05 |
| `aviso-whatsapp` | Link click-to-chat `wa.me` del turno | C-06 |
| `agenda` | Agenda del día por profesional | C-07 |
| `interfaz-gestion` | Login, panel y pantallas de gestión | C-09 |
| `interfaz-turnos` | Pantallas de turnos y agenda | C-10 |

Los 10 changes que las generaron están archivados en [`openspec/changes/archive/`](openspec/changes/archive/). El change evaluado con el ciclo OPSX completo es `crear-turno-sin-solapamientos` (C-02).

## Limitaciones conocidas

Quedan anotadas para una próxima etapa; ninguna rompe una regla de las specs.

- Un odontólogo puede abrir el detalle de cualquier turno (en la vista y en `GET /api/turnos/{id}`); la agenda sí está limitada a la suya.
- Si se abre en el navegador un turno que no existe (`/turnos/999`), la respuesta es el error JSON de la API en lugar de una pantalla.
- `/api/**` no exige token CSRF aunque la sesión viaja en una cookie. El riesgo es bajo porque la API recibe solo JSON, pero si se expone a otros clientes conviene pasar a token o exigir CSRF.
- Sin recordatorios automáticos, ficha clínica ni cobros: están en el backlog de [`CHANGES.md`](CHANGES.md).

## Mapa del repositorio

| Ruta | Qué hay |
|---|---|
| [`AGENTS.md`](AGENTS.md) / [`CLAUDE.md`](CLAUDE.md) | Instrucciones y **reglas duras** para personas y agentes. Leer primero. |
| [`docs/discovery/`](docs/discovery/) | Investigación de mercado: informe A-D (`.md` y `.pdf`) y fichas de 19 competidores |
| [`knowledge-base/`](knowledge-base/) | Base de conocimiento: visión, actores, modelo de datos, **reglas de negocio**, arquitectura, decisiones, preguntas abiertas |
| [`CHANGES.md`](CHANGES.md) | Roadmap: 11 changes, dependencias, asignación por integrante, reglas anti-colisión |
| [`openspec/`](openspec/) | Specs vigentes (`specs/`) y changes en curso o archivados (`changes/`) |
| [`design-system/`](design-system/) | Sistema de diseño aprobado: tokens, componentes y logo (los mockups de cada change están en su carpeta archivada) |
| `src/main/java/ar/odonto/turnos/` | Código: `domain` (modelo, puertos, errores, reglas), `application` (casos de uso), `infrastructure`, `web` |
| `src/test/java/ar/odonto/turnos/` | Tests; `support/` tiene `RelojDePrueba` y `DatosDePrueba` (datos ficticios) |

## Flujo de trabajo del equipo

1. Buscar tu change en [`CHANGES.md`](CHANGES.md) y leer los archivos de "Leer antes".
2. Crear la rama desde `main` actualizado: `git switch -c feat/<nombre-del-change>`.
3. Ciclo OPSX: `openspec-explore` (opcional) → `openspec-propose` → revisar la propuesta → `openspec-apply-change` → tests en verde → `openspec-archive-change`.
4. Agregar código **solo en el subpaquete de tu change**. Los archivos compartidos (modelo, puertos, `CodigoError`, configuración, `pom.xml`) se piden al líder (reglas E1 y J5).
5. Abrir un PR a `main` con `.\mvnw.cmd test` en verde. El líder revisa y hace *squash and merge*.

Reglas que no se negocian: **nunca datos reales de pacientes** (todo ficticio), nunca secretos en el repo, y commits con tu propia identidad de git.
