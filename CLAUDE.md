# Turnos Odontología: Instrucciones para Agentes

> Este archivo (y su copia `CLAUDE.md`) es lo PRIMERO que todo agente lee al entrar al repo.
> Generado a partir de `knowledge-base/`, `CHANGES.md` y `.atl/skill-registry.md` (2026-11-20). Si se edita, hay que mantener **ambos archivos idénticos**.

Agenda de turnos para consultorios odontológicos argentinos de 1 a 5 profesionales que **rechaza choques por profesional y por sillón a la vez**, con margen de 5 minutos, sobreturno controlado y cancelación con 6 h de anticipación. Equipo: Santiago (líder y dueño del repo), Joaquin, Romero, Castillo, Adriano y Guiretti. Entrega: **2026-11-25**.

---

## Stack Tecnológico

| Capa | Tecnologías | Versión mínima |
|---|---|---|
| Lenguaje | Java | 17 (LTS) |
| Framework | Spring Boot (WebMVC, Validation, Data JPA, Security, Thymeleaf) | 4.2.0 |
| Build | Maven con Maven Wrapper (`mvnw` / `mvnw.cmd`) | 3.9 (vía wrapper) |
| Persistencia | Spring Data JPA + H2 en memoria (PostgreSQL como destino futuro) | H2 2.x |
| Vistas | Thymeleaf (renderizado en el servidor) | la de Spring Boot 4 |
| Seguridad | Spring Security, usuario y contraseña con BCrypt (sesión o JWT: ver IN-01) | 7.x |
| Tests | JUnit 5, AssertJ, Mockito, Spring Boot Test | JUnit 5.10 |
| Tiempo | `java.time` con `Clock` inyectable, zona `America/Argentina/Buenos_Aires` | — |

Requisito: **JDK 17 instalado** (PA-01). Comando de tests: `./mvnw test` (Windows: `mvnw.cmd test`).
Detalle completo: [knowledge-base/02_descripcion_general.md](knowledge-base/02_descripcion_general.md)

---

## Base de Conocimiento

La fuente de verdad del dominio vive en `knowledge-base/`. **Leé el archivo relevante ANTES de implementar.**

| Archivo | Cuándo leerlo |
|---|---|
| [01_vision_y_objetivos.md](knowledge-base/01_vision_y_objetivos.md) | Propósito, alcance del MVP, fuera de alcance |
| [03_actores_y_roles.md](knowledge-base/03_actores_y_roles.md) | Roles ADMIN, SECRETARIA y ODONTOLOGO; matriz RBAC |
| [04_modelo_de_datos.md](knowledge-base/04_modelo_de_datos.md) | Entidades, ERD, seed ficticio |
| [05_reglas_de_negocio.md](knowledge-base/05_reglas_de_negocio.md) | **Reglas RN-TU / RN-CA / RN-AG / RN-WA / RN-AU / RN-DP con sus códigos de error** |
| [06_funcionalidades.md](knowledge-base/06_funcionalidades.md) | Historias US-NNN y criterios de aceptación |
| [07_flujos_principales.md](knowledge-base/07_flujos_principales.md) | Flujos de punta a punta y ejemplos de borde (10:04 vs 10:05) |
| [08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md) | Capas, paquetes, seguridad, variables de entorno, testing |
| [09_decisiones_y_supuestos.md](knowledge-base/09_decisiones_y_supuestos.md) | Decisiones DD-01 a DD-09 y supuestos SU-01 a SU-06 |
| [10_preguntas_abiertas.md](knowledge-base/10_preguntas_abiertas.md) | ⚠️ Inconsistencias y preguntas a resolver ANTES de codear |

> ⚠️ Antes del primer change, resolver las preguntas de prioridad **Alta** de `10_preguntas_abiertas.md`: PA-01 (instalar JDK 17), PA-03 (margen con sobreturno) y PA-06 (concurrencia).
> El Discovery (mercado, competidores, MVP) está en `docs/discovery/`.

---

## Skills Disponibles

| Agente | Rol | Skills que carga |
|---|---|---|
| **Backend Core** | Dominio de agenda, casos de uso, JPA, API REST | `java-springboot`, `api-design-principles`, `tdd` |
| **Testing** | Un test por escenario del spec, `Clock` fijo, casos borde | `java-junit`, `tdd` |
| **Seguridad** | Spring Security, RBAC, CSRF / sesión vs JWT (C-05) | `springboot-security` |
| **Orquestación SDD** | Ciclo OPSX por change | `openspec-explore`, `openspec-propose`, `openspec-apply-change`, `openspec-update-change`, `openspec-sync-specs`, `openspec-archive-change` |
| **UI (solo desde C-09)** | Vistas Thymeleaf; política visual `system-and-mockups` (mockup aprobado por cada cambio visual) | `design-system`, `emil-design-eng` |

Cargá la skill correspondiente al contexto ANTES de escribir código. **No usar** `fastify-best-practices` ni `vitest`, que son de otro stack.

> Los compact rules de cada skill los resuelve el orquestador desde `.atl/skill-registry.md` (generado por `skill-registry`). En este proyecto el registry **sí está versionado**, porque la consigna lo exige como entregable. Esta tabla solo mapea skill→rol.
> Las skills de Java/Spring son globales: cada integrante las instala con `npx -y skills add github/awesome-copilot --skill java-springboot java-junit -g -a claude-code -y`, `npx -y skills add affaan-m/ecc --skill springboot-security -g -a claude-code -y` y `npx -y skills add wshobson/agents --skill api-design-principles -g -a claude-code -y`. Las de OpenSpec ya vienen en el repo (`.claude/skills/`).

---

## Roadmap de Changes

El plan completo está en [CHANGES.md](CHANGES.md). Resumen:

- **Total**: 11 changes en 4 fases (ola 0 → ola 1 en paralelo → ola 2 → cierre).
- **Camino crítico** (4): `C-01 scaffold-proyecto → C-02 crear-turno-sin-solapamientos → C-08 cancelar-turno → C-11 integracion-y-verificacion-final`.
- **Primer change**: `C-01 scaffold-proyecto` (Santiago). Es dueño de **todos** los archivos compartidos y bloquea al resto.
- **Change evaluado** (ciclo OPSX completo): `C-02 crear-turno-sin-solapamientos` (Santiago).

| Integrante | Changes |
|---|---|
| Santiago (líder) | C-01, C-02, C-11 |
| Joaquin | C-03 `configurar-recursos`, C-10 `ui-turnos-y-agenda` |
| Romero | C-04 `gestionar-pacientes`, C-08 `cancelar-turno` |
| Castillo | C-05 `autenticacion-y-roles` |
| Adriano | C-06 `link-whatsapp-turno` |
| Guiretti | C-07 `agenda-del-dia`, C-09 `ui-base-y-gestion` |

**Antes de cualquier propuesta** (`openspec-propose`): leé [CHANGES.md](CHANGES.md), revisá las dependencias de tu change, sus archivos de "Leer antes" y las **reglas anti-colisión**.

---

## Reglas Duras (específicas del proyecto)

> El líder tiene en su máquina un archivo global de instrucciones (orquestador OPSX, governance, Engram, diseño) que este proyecto **hereda**. Como el resto del equipo no lo tiene, el resumen de lo esencial está en la sección G, más abajo.

Son contrato: romperlas es un defecto.

### Dominio
- **D1.** NUNCA usar datos reales de pacientes (en el repo, el seed, los tests, las capturas o los prompts al agente) → usar datos ficticios (DNI `0000000X`, teléfono `549110000000X`, nombres tipo "Paciente Uno"). Marco legal: Leyes 25.326 y 26.529.
- **D2.** NUNCA registrar en logs el DNI, el teléfono ni el nombre completo de un paciente → loguear solo IDs.
- **D3.** NUNCA cambiar una regla RN-TU o RN-CA en el código sin actualizar `knowledge-base/05_reglas_de_negocio.md` y la spec del change → volver a *propose*.
- **D4.** NUNCA usar `LocalDateTime.now()` ni la zona horaria del sistema → usar el `Clock` inyectado con zona `America/Argentina/Buenos_Aires`.

### Stack Java/Spring
- **J1.** NUNCA poner lógica de negocio en controladores ni anotaciones de Spring o JPA en `domain/rules` → las reglas van en Java puro, testeables sin levantar Spring.
- **J2.** NUNCA devolver entidades JPA desde la API → usar DTOs o records; los errores van como `ProblemDetail` con el campo `codigo` tomado de `CodigoError` (409 para choques, 422 para otras reglas).
- **J3.** NUNCA inyectar dependencias por campo (`@Autowired` en atributos) → usar inyección por constructor.
- **J4.** NUNCA dar una tarea por hecha con tests en rojo, `@Disabled` o `-DskipTests` → `./mvnw test` en verde, con al menos un test por escenario del spec.
- **J5.** NUNCA subir versiones de Java o Spring Boot ni agregar dependencias desde un change → pedírselo al líder (`pom.xml` es de C-01).

### Equipo y repositorio
- **E1.** NUNCA editar un archivo compartido (los de C-01: `pom.xml`, `domain/model`, `domain/port`, `domain/error`, `infrastructure/persistence`, `infrastructure/config`, `web/api/error`, `application*.yml`, `README.md`) desde otro change → pedir el cambio al líder (reglas anti-colisión de `CHANGES.md`).
- **E2.** NUNCA commitear directo a `main` ni hacer push sin que el integrante lo pida → trabajar en la rama `feat/<change>` y abrir PR a `main`; el líder hace *squash and merge*.
- **E3.** NUNCA versionar secretos (contraseñas, `JWT_SECRET`, `.env`) → usar variables de entorno; `.gitignore` ya cubre `.env` y `target/`.
- **E4.** Commits con formato *conventional commits* en español (`feat(turnos): ...`, `test(agenda): ...`), cada integrante con **su propia identidad de git**, para que la participación sea demostrable.
- **E5.** NUNCA implementar en *apply* algo que no está en la spec aprobada → volver a *propose*.

### G. Resumen del global para el equipo
- **G1.** El estado de los artefactos lo da **siempre** el CLI de OpenSpec (`openspec status`, `openspec list`); nunca se adivina ni se crea `openspec/` a mano.
- **G2.** Governance por dominio: **autenticación y seguridad (C-05)** = el agente analiza y propone, y **no escribe código sin aprobación humana explícita**. Configuración y datos = propone y espera revisión. Lógica de negocio = implementa con checkpoints. CRUD simples = autonomía si los tests pasan.
- **G3.** Si el agente tiene Engram disponible, guarda las decisiones, los bugs resueltos y los hallazgos no obvios (proyecto `grupo-3-bazzana`).
- **G4.** Visual: la política es `system-and-mockups` (`design-system/README.md`, revisión 1, aprobada en C-09 el 2026-11-23). Cada change con pantallas necesita mockups aprobados por el líder antes del apply.

---

## Flujo de Trabajo

```
1. Leer la KB relevante (knowledge-base/)                → entender el dominio y las reglas
2. Identificar tu change en CHANGES.md                   → respetar dependencias y anti-colisión
3. git switch -c feat/<change> desde main actualizado
4. openspec-explore (si hay dudas) → openspec-propose    → proposal + design + specs + tasks
5. openspec-apply-change (cargando las skills de tu rol) → solo lo que dice la spec
6. Verify: ./mvnw test en verde (un test por escenario)
7. openspec-archive-change + marcar [x] en CHANGES.md   → PR a main; el líder revisa y mergea
```

Aplicar TODAS las reglas duras en cada paso. Ante un conflicto entre la KB y este archivo, prevalecen las reglas duras.
