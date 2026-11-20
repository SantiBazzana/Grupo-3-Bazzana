# base-del-sistema Specification

## Purpose

Define el comportamiento transversal que comparten todos los módulos del sistema de turnos: el formato y los códigos de error de la API, el health check, la zona horaria y el reloj del sistema, el arranque sin infraestructura externa y los datos de ejemplo ficticios del entorno de desarrollo.

## Requirements

### Requirement: Formato uniforme de errores
Toda respuesta de error de la API SHALL usar el formato Problem Details (RFC 7807), con tipo de contenido `application/problem+json` y los campos `status`, `title`, `detail` y `codigo`. El campo `codigo` MUST ser uno de los códigos del catálogo del sistema.

#### Scenario: Violación de una regla de negocio
- **WHEN** una operación rechaza una solicitud por una regla de negocio con código `CHOQUE_SILLON`
- **THEN** la respuesta tiene estado 409, tipo `application/problem+json` y el campo `codigo` igual a `CHOQUE_SILLON`

#### Scenario: Error sin datos sensibles
- **WHEN** se produce un error inesperado no contemplado por el catálogo
- **THEN** la respuesta tiene estado 500 y `codigo` igual a `ERROR_INTERNO`
- **AND** el cuerpo no incluye la traza de la excepción ni datos personales de pacientes

### Requirement: Catálogo de códigos de error con estado HTTP fijo
El sistema SHALL definir un catálogo único de códigos de error, cada uno con un estado HTTP fijo: 409 para los choques (`CHOQUE_PROFESIONAL`, `CHOQUE_SILLON`, `CHOQUE_PACIENTE`, `DATO_DUPLICADO`), 404 para `RECURSO_INEXISTENTE`, 400 para `ENTRADA_INVALIDA`, 401 para `NO_AUTENTICADO`, 403 para `ACCESO_DENEGADO` y 422 para las demás reglas de negocio.

#### Scenario: Cada código responde con su estado
- **WHEN** se emite cada código del catálogo
- **THEN** el estado HTTP de la respuesta es el asignado a ese código: 409, 404, 400, 401, 403 o 422 según el caso

#### Scenario: Códigos de las reglas de la base de conocimiento
- **WHEN** se consulta el catálogo
- **THEN** contiene al menos `CHOQUE_PROFESIONAL`, `CHOQUE_SILLON`, `SILLON_REQUERIDO`, `CHOQUE_PACIENTE`, `TURNO_EN_PASADO`, `FUERA_DE_HORARIO`, `RECURSO_INEXISTENTE`, `RECURSO_INACTIVO`, `CANCELACION_TARDIA`, `TURNO_YA_CANCELADO`, `PACIENTE_SIN_TELEFONO`, `ENTRADA_INVALIDA`, `DATO_DUPLICADO`, `DURACION_INVALIDA`, `HORARIO_INVALIDO`, `NO_AUTENTICADO`, `ACCESO_DENEGADO` y `ERROR_INTERNO`

### Requirement: Entrada mal formada
Cuando una solicitud no se puede interpretar (cuerpo JSON inválido, tipo de dato incorrecto o campo obligatorio faltante), el sistema SHALL responder 400 con `codigo` igual a `ENTRADA_INVALIDA` e indicar qué campo falló, sin ejecutar ninguna regla de negocio.

#### Scenario: Campo obligatorio ausente
- **WHEN** una solicitud omite un campo marcado como obligatorio
- **THEN** la respuesta tiene estado 400, `codigo` igual a `ENTRADA_INVALIDA` y menciona el campo faltante

#### Scenario: JSON ilegible
- **WHEN** el cuerpo de la solicitud no es JSON válido
- **THEN** la respuesta tiene estado 400 y `codigo` igual a `ENTRADA_INVALIDA`

### Requirement: Health check público
El sistema SHALL exponer `GET /actuator/health`, accesible sin autenticación, que responde 200 con estado `UP` cuando la aplicación y la base de datos están disponibles.

#### Scenario: Aplicación sana
- **WHEN** se consulta `GET /actuator/health` sin credenciales
- **THEN** la respuesta tiene estado 200 y el campo `status` igual a `UP`

### Requirement: Zona horaria y reloj del sistema
Todas las fechas y horas de negocio SHALL interpretarse en la zona `America/Argentina/Buenos_Aires`, sin importar la zona horaria del servidor. El "ahora" del sistema MUST salir de un único reloj configurable, para que las reglas que dependen del tiempo se puedan verificar con un instante fijo.

#### Scenario: Servidor en otra zona horaria
- **GIVEN** que el servidor está configurado en la zona UTC
- **WHEN** el sistema calcula el instante actual de negocio
- **THEN** el resultado está expresado en la hora de `America/Argentina/Buenos_Aires`

#### Scenario: Reloj fijo para verificación
- **GIVEN** que el reloj del sistema se configura en un instante fijo
- **WHEN** cualquier regla consulta el "ahora"
- **THEN** obtiene exactamente ese instante

### Requirement: Parámetros de negocio configurables
El margen mínimo entre turnos de un mismo recurso SHALL valer 5 minutos y la anticipación mínima para cancelar SHALL valer 6 horas por defecto. Los dos MUST poder cambiarse por configuración, sin modificar el código.

#### Scenario: Valores por defecto
- **WHEN** la aplicación arranca sin configuración específica
- **THEN** el margen entre turnos es 5 minutos y la anticipación mínima de cancelación es 6 horas

#### Scenario: Valor configurado
- **WHEN** la aplicación arranca con el margen configurado en 10 minutos
- **THEN** el margen que usa el sistema es 10 minutos

### Requirement: Arranque sin infraestructura externa
El sistema SHALL arrancar y ejecutar su suite de tests con un JDK 17 instalado y el Maven Wrapper del repositorio, sin instalar una base de datos, Maven ni otros servicios, usando una base de datos en memoria.

#### Scenario: Tests desde un clon limpio
- **GIVEN** un clon limpio del repositorio en una máquina con JDK 17
- **WHEN** se ejecuta `./mvnw test` (o `mvnw.cmd test` en Windows)
- **THEN** la suite compila y termina en verde sin requerir servicios externos

### Requirement: Datos de ejemplo ficticios solo en desarrollo
Con el perfil `dev`, el sistema SHALL cargar al iniciar datos de ejemplo **ficticios**: un consultorio con horario de lunes a viernes de 08:00 a 20:00, dos sillones, dos profesionales, las prestaciones Control (30 min), Limpieza (45 min), Conducto (60 min) y Extracción (40 min), y tres pacientes. Sin el perfil `dev`, la base MUST arrancar vacía.

#### Scenario: Arranque con perfil dev
- **WHEN** la aplicación arranca con el perfil `dev`
- **THEN** existen el consultorio con su horario, 2 sillones, 2 profesionales, 4 prestaciones con sus duraciones y 3 pacientes

#### Scenario: Arranque sin perfil dev
- **WHEN** la aplicación arranca sin el perfil `dev` (por ejemplo, durante los tests)
- **THEN** no existe ningún dato precargado

#### Scenario: Datos de pacientes ficticios
- **WHEN** se inspeccionan los pacientes de ejemplo
- **THEN** sus DNI y teléfonos siguen los patrones ficticios `0000000X` y `549110000000X`, y no corresponden a personas reales

### Requirement: Usuarios de ejemplo sin contraseñas versionadas
Con el perfil `dev`, el sistema SHALL crear los usuarios de ejemplo `admin`, `secretaria` y `ana` (odontóloga vinculada a un profesional) **solo** si sus contraseñas se proveen por variables de entorno. Las contraseñas MUST guardarse con hash y nunca aparecer en el repositorio ni en los logs.

#### Scenario: Contraseñas provistas
- **GIVEN** que las variables de entorno de contraseña de seed están definidas
- **WHEN** la aplicación arranca con el perfil `dev`
- **THEN** existen los tres usuarios con sus roles y sus contraseñas guardadas como hash

#### Scenario: Contraseñas ausentes
- **GIVEN** que las variables de entorno de contraseña de seed no están definidas
- **WHEN** la aplicación arranca con el perfil `dev`
- **THEN** no se crea ningún usuario de ejemplo y el log indica cómo habilitarlos, sin mostrar ninguna contraseña
