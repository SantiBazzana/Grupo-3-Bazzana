# Descripción General

## Stack tecnológico

| Capa | Tecnologías | Versión mínima |
|---|---|---|
| Lenguaje | Java | 17 (LTS) |
| Framework | Spring Boot (WebMVC, Validation, Data JPA, Security, Thymeleaf) | 4.2.0 |
| Build | Maven, con Maven Wrapper (`mvnw`) versionado en el repo | 3.9 (vía wrapper) |
| Persistencia | Spring Data JPA + **H2 en memoria** (perfil por defecto); PostgreSQL como destino futuro, con solo un cambio de configuración | H2 2.x |
| Vistas | Thymeleaf (renderizado en el servidor) | la de Spring Boot 4 |
| Seguridad | Spring Security con autenticación por usuario y contraseña; JWT (ver IN-01) | 7.x |
| Tests | JUnit 5, AssertJ, Mockito, Spring Boot Test (`@WebMvcTest`, `@DataJpaTest`) | JUnit 5.10 |
| Tiempo | `java.time` con `Clock` inyectable; zona `America/Argentina/Buenos_Aires` | — |

**Requisito de entorno**: JDK 17 instalado. Al 2026-11-20, la máquina del líder tiene Node, pero **no** tiene Java, Maven ni Gradle (ver `10_preguntas_abiertas.md`, PA-01).

## Arquitectura general

Monolito **Spring Boot** en capas, con el dominio aislado (estilo hexagonal liviano):

```
 Navegador (Thymeleaf)        Cliente HTTP (tests, Postman)
          │                              │
          ▼                              ▼
 ┌──────────────────────────────────────────────┐
 │  web: controladores MVC (vistas) y REST (API)│  ← Spring Security (roles)
 ├──────────────────────────────────────────────┤
 │  application: casos de uso (CrearTurno,      │  ← @Transactional
 │  CancelarTurno, ConsultarAgenda, ...)        │
 ├──────────────────────────────────────────────┤
 │  domain: entidades + reglas de agenda        │  ← Java puro, sin Spring
 │  (ValidadorDeChoques, Turno, Intervalo...)   │
 ├──────────────────────────────────────────────┤
 │  infrastructure: repositorios JPA (H2),      │
 │  generador de link wa.me, Clock              │
 └──────────────────────────────────────────────┘
```

**Justificación**:
- Las reglas de choque son el valor del producto. Viven en el dominio, en Java puro, y se testean con JUnit sin levantar Spring. Ver DD-03.
- Un solo deployable, porque el equipo y el plazo no justifican microservicios.
- H2 en memoria permite clonar y correr los tests sin instalar una base de datos.

## Integraciones externas

| Servicio | Propósito | Tipo | Estado |
|---|---|---|---|
| WhatsApp click-to-chat | Avisar al paciente de su turno: se genera `https://wa.me/<telefono>?text=<mensaje>` y la secretaria lo abre a mano | URL (sin API, sin costo, sin credenciales) | **MVP** |
| WhatsApp Business API (Meta) | Recordatorios y confirmación automáticos | REST + webhook | Próxima etapa (interfaz `NotificadorDeTurnos`) |
| Mercado Pago | Seña y cobros | SDK/REST | Próxima etapa |
| ARCA (ex AFIP) | Factura electrónica | Web service | Próxima etapa |
| PUCO / obras sociales | Verificación de cobertura | Web service | Etapa posterior |

Sin integraciones reales en el MVP: no hay claves ni secretos de terceros en el repositorio.

## API REST (resumen)

Prefijo `/api`. Los errores siguen el formato *Problem Details* (RFC 7807), con un campo `codigo` igual al código de la regla violada (ver `05_reglas_de_negocio.md`).

| Recurso | Método y ruta | Descripción | Roles |
|---|---|---|---|
| Auth | `POST /api/auth/login` | Usuario y contraseña → token | público |
| Turnos | `POST /api/turnos` | Crear turno (aplica RN-TU-*) | SECRETARIA, ADMIN |
| Turnos | `POST /api/turnos/{id}/cancelacion` | Cancelar turno (RN-CA-*) | SECRETARIA, ADMIN |
| Turnos | `GET /api/turnos/{id}` | Detalle de un turno | SECRETARIA, ADMIN, ODONTOLOGO (propio) |
| Turnos | `GET /api/turnos/{id}/whatsapp-link` | Link wa.me con el mensaje del turno | SECRETARIA, ADMIN |
| Agenda | `GET /api/agenda?profesionalId={id}&fecha={yyyy-MM-dd}` | Agenda del día de un odontólogo | SECRETARIA, ADMIN, ODONTOLOGO (propia) |
| Profesionales | `GET/POST/PUT /api/profesionales` | Configuración | ADMIN (lectura: todos) |
| Sillones | `GET/POST/PUT /api/sillones` | Configuración | ADMIN (lectura: todos) |
| Prestaciones | `GET/POST/PUT /api/prestaciones` | Configuración (duración en minutos) | ADMIN (lectura: todos) |
| Pacientes | `GET/POST/PUT /api/pacientes` | Alta y edición de pacientes | SECRETARIA, ADMIN |
| Horario | `GET/PUT /api/consultorio/horario` | Horario de atención | ADMIN |

Códigos HTTP:
- `201` creado.
- `409 Conflict` para los choques (RN-TU-02, RN-TU-03, RN-TU-05).
- `422 Unprocessable Entity` para las demás violaciones de reglas (pasado, fuera de horario, cancelación tardía).
- `400` para entradas mal formadas.
- `401` y `403` para seguridad.
