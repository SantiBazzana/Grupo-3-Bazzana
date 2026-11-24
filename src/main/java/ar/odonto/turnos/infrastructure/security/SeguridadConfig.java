package ar.odonto.turnos.infrastructure.security;

import ar.odonto.turnos.domain.model.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Cadena de seguridad propia de C-05. Reemplaza a la permisiva de C-01
 * ({@code PermisivaSecurityAutoConfiguration.cadenaPermisiva}) mediante
 * {@code @ConditionalOnMissingBean}: como esta clase declara un bean
 * {@link SecurityFilterChain}, el de C-01 se retira sin editar archivos compartidos.
 * <p>
 * Aplica autenticación y autorización por ruta según la matriz RBAC de
 * {@code knowledge-base/03_actores_y_roles.md}. Rutas públicas sin autenticación;
 * todo lo demás requiere usuario autenticado (RN-AU-01, RN-AU-03).
 * <p>
 * CSRF habilitado para los forms Thymeleaf de login y gestión (C-09): Thymeleaf + el
 * procesador de Spring Security agrega el token a los forms con {@code th:action}. La API
 * REST stateless ({@code /api/**}) queda exenta porque sus clientes envían JSON sin token;
 * las vistas no consumen esos endpoints, consumen los casos de uso directamente (D3).
 * No se emite JWT (IN-01): la autenticación se maneja con sesión de Spring Security para las
 * vistas y para el endpoint {@code POST /api/auth/login}.
 */
@Configuration(proxyBeanMethods = false)
public class SeguridadConfig {

    private static final String[] PUBLICAS = {
            "/login",
            "/api/auth/login",
            "/css/**",
            "/js/**",
            "/img/**",
            "/actuator/health",
            "/h2-console/**"
    };

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (03_actores_y_roles.md §Rutas públicas)
                        .requestMatchers(PUBLICAS).permitAll()
                        // RBAC por ruta según knowledge-base/03_actores_y_roles.md
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        .requestMatchers(HttpMethod.POST, "/api/pacientes")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        .requestMatchers(HttpMethod.PUT, "/api/pacientes/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        .requestMatchers(HttpMethod.GET, "/api/profesionales/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        .requestMatchers(HttpMethod.POST, "/api/profesionales")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/profesionales/**")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/sillones/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        .requestMatchers(HttpMethod.POST, "/api/sillones")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/sillones/**")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/prestaciones/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        .requestMatchers(HttpMethod.POST, "/api/prestaciones")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/prestaciones/**")
                                .hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/consultorio/horario")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        .requestMatchers(HttpMethod.PUT, "/api/consultorio/horario")
                                .hasRole(Rol.ADMIN.name())
                        // Link de WhatsApp: solo ADMIN y SECRETARIA (matriz RBAC; RN-WA).
                        // Regla específica ANTES de la general GET /api/turnos/** (first match wins).
                        .requestMatchers(HttpMethod.GET, "/api/turnos/*/whatsapp-link")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        // Turnos: lectura (todos autenticados); crear/cancelar (ADMIN / SECRETARIA).
                        .requestMatchers(HttpMethod.GET, "/api/turnos/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        .requestMatchers(HttpMethod.POST, "/api/turnos/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        // Vistas de gestion (C-09): lectura segun la matriz RBAC por ruta. La
                        // secretaria y el odontologo solo leen configuracion; los pacientes quedan
                        // reservados a ADMIN y SECRETARIA (knowledge-base/03_actores_y_roles.md).
                        .requestMatchers(HttpMethod.GET, "/gestion/pacientes/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        .requestMatchers(HttpMethod.POST, "/gestion/pacientes/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name())
                        .requestMatchers(HttpMethod.GET, "/gestion/profesionales/**", "/gestion/sillones/**",
                                "/gestion/prestaciones/**", "/gestion/horario/**")
                                .hasAnyRole(Rol.ADMIN.name(), Rol.SECRETARIA.name(), Rol.ODONTOLOGO.name())
                        // Solo ADMIN da de alta o modifica la configuracion del consultorio (C en la matriz).
                        .requestMatchers(HttpMethod.POST, "/gestion/profesionales/**", "/gestion/sillones/**",
                                "/gestion/prestaciones/**", "/gestion/horario/**")
                                .hasRole(Rol.ADMIN.name())
                        // Todo lo demas requiere autenticacion (incluye /panel).
                        .anyRequest().authenticated()
                )
                // /login es la vista Thymeleaf de login (C-09); el procesamiento del form vive en POST /login.
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/panel"))
                // CSRF habilitado para los forms Thymeleaf de login y gestion. La API REST es
                // stateless y recibe JSON sin token (sus clientes/tests no envian _csrf), asi que
                // queda exenta; las vistas consumen los casos de uso (D3), no endpoints /api/**.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
                // Errores de seguridad: la API responde Problem Detail con `codigo`; las vistas
                // (y cualquier otra ruta) redirigen a /login, preservando el pedido original.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(new RestAuthenticationEntryPoint(),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                                PathPatternRequestMatcher.withDefaults().matcher("/**"))
                        .defaultAccessDeniedHandlerFor(new RestAccessDeniedHandler(),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAccessDeniedHandlerFor(new AccessDeniedHandlerImpl(),
                                PathPatternRequestMatcher.withDefaults().matcher("/**"))
                );
        return http.build();
    }

    @Bean
    HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
