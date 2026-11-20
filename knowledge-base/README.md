# Turnos Odontología: Base de Conocimiento

Base de conocimiento generada a partir del Discovery (`docs/discovery/`) y de la ronda de decisiones técnicas del equipo (2026-11-20).

## Índice de Archivos

| Archivo | Contenido |
|---------|-----------|
| [01_vision_y_objetivos.md](01_vision_y_objetivos.md) | Propósito, objetivos por actor, alcance del MVP y fuera de alcance |
| [02_descripcion_general.md](02_descripcion_general.md) | Stack (Java 17, Spring Boot 4, H2, Thymeleaf), arquitectura, integraciones, API REST |
| [03_actores_y_roles.md](03_actores_y_roles.md) | Secretaria, odontólogo y admin; matriz RBAC; rutas públicas |
| [04_modelo_de_datos.md](04_modelo_de_datos.md) | Entidades (Turno, Profesional, Sillón, Prestación, Paciente…), ERD, seed ficticio |
| [05_reglas_de_negocio.md](05_reglas_de_negocio.md) | RN-TU (choques, margen, sobreturno), RN-CA (cancelación 6 h), RN-AG, RN-WA, RN-AU, RN-DP |
| [06_funcionalidades.md](06_funcionalidades.md) | Épicas e historias US-NNN con criterios de aceptación y backlog |
| [07_flujos_principales.md](07_flujos_principales.md) | Crear turno, cancelar, agenda del día, WhatsApp, login |
| [08_arquitectura_propuesta.md](08_arquitectura_propuesta.md) | Patrones, estructura de paquetes, seguridad, variables de entorno, testing |
| [09_decisiones_y_supuestos.md](09_decisiones_y_supuestos.md) | DD-01 a DD-09 y SU-01 a SU-06 |
| [10_preguntas_abiertas.md](10_preguntas_abiertas.md) | Inconsistencias, preguntas priorizadas y datos "No evidenciado" del Discovery |

## Quick Start para Desarrolladores

1. Entender el dominio → [01](01_vision_y_objetivos.md), [03](03_actores_y_roles.md)
2. Entender los datos → [04](04_modelo_de_datos.md)
3. Entender las reglas → [05](05_reglas_de_negocio.md) (**leer antes de tocar turnos**)
4. Entender la arquitectura → [02](02_descripcion_general.md), [08](08_arquitectura_propuesta.md)
5. Implementar → [07](07_flujos_principales.md), [06](06_funcionalidades.md)
6. Antes de codificar → [10](10_preguntas_abiertas.md) (PA-01: instalar el JDK 17)

## Resumen Ejecutivo

Agenda de turnos para consultorios odontológicos argentinos de 1 a 5 profesionales que **rechaza los choques por profesional y por sillón a la vez**, con margen de 5 minutos, sobreturno controlado y cancelación con 6 horas de anticipación. Es un vacío que ninguno de los 19 competidores relevados documenta. Monolito Java 17 + Spring Boot 4 con H2 y Thymeleaf; las reglas viven en un dominio Java puro y se testean una por escenario. El primer change es "crear turno sin solapamientos"; la entrega es el 2026-11-25.
