# interfaz-gestion Specification

## Purpose

Define la base de la interfaz web de gestión del consultorio: layout de Thymeleaf, vista de login y pantallas de gestión con control de visibilidad por rol.

## Requirements

### Requirement: Interfaz base de gestión con layout, login y pantallas

El sistema SHALL proveer una interfaz basada en Thymeleaf con layout base, recursos estáticos, vista de login y pantallas de gestión que consumen los casos de uso existentes para profesionales, sillones, prestaciones, horario y pacientes.

#### Scenario: Inicio de sesión exitoso en la vista de login (feliz)

- **GIVEN** un usuario con credenciales válidas
- **WHEN** completa el formulario de login de la vista
- **THEN** la vista inicia la sesión y redirige al panel de gestión

#### Scenario: Alta de paciente con DNI duplicado muestra el error de la regla (error de negocio)

- **GIVEN** la vista de gestión de pacientes
- **WHEN** intenta registrar un paciente con DNI duplicado
- **THEN** la vista muestra el error del código de regla correspondiente

#### Scenario: Acceso a pantalla de gestión sin permiso (borde)

- **GIVEN** un usuario sin el rol requerido
- **WHEN** intenta acceder a una pantalla de gestión
- **THEN** la vista responde con acceso denegado

### Requirement: Protección CSRF en los formularios de la interfaz

El sistema SHALL exigir un token CSRF válido en los formularios Thymeleaf de login y gestión y MUST rechazar los POST de vistas sin token válido.

#### Scenario: Formulario de login con token CSRF procesa el POST (feliz)

- **GIVEN** un usuario autenticado en el formulario de login con token CSRF válido
- **WHEN** envía el formulario
- **THEN** el POST se procesa correctamente

#### Scenario: POST de vista sin token CSRF es rechazado (borde)

- **GIVEN** una petición POST de vista sin token CSRF válido
- **WHEN** se realiza
- **THEN** la respuesta es rechazada con error de seguridad

### Requirement: Visibilidad de las pantallas de gestión según el rol

El sistema SHALL mostrar en las pantallas de gestión solo las secciones permitidas por el rol autenticado.

#### Scenario: Secretaría ve la sección de pacientes (feliz)

- **GIVEN** un usuario autenticado con rol SECRETARIA
- **WHEN** consulta la pantalla de pacientes
- **THEN** la vista muestra la sección correspondiente

#### Scenario: Odontólogo sin permiso accede a pantalla de gestión restringida (borde)

- **GIVEN** un usuario autenticado con rol ODONTOLOGO
- **WHEN** intenta acceder a una pantalla de gestión restringida
- **THEN** la vista responde con acceso denegado
