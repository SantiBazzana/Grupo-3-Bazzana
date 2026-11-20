package ar.odonto.turnos.infrastructure.config;

import ar.odonto.turnos.domain.model.Solicitante;
import ar.odonto.turnos.domain.port.UsuarioActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad PROVISORIA y permisiva hasta que exista C-05 (design D8 de C-01).
 * <p>
 * Es una auto-configuración (registrada en {@code META-INF/spring/...AutoConfiguration.imports}) y no una
 * {@code @Configuration} común: las auto-configuraciones se evalúan DESPUÉS de la configuración de la aplicación,
 * así que cuando C-05 declare su propio {@link SecurityFilterChain}, {@link UsuarioActual} y
 * {@link UserDetailsService} en {@code infrastructure/security}, estos beans se desactivan solos, sin editar
 * este archivo. Se ordena antes de las auto-configuraciones de Spring Security para que las de Spring no
 * agreguen su cadena por defecto ni generen una contraseña.
 */
@AutoConfiguration(before = {ServletWebSecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class PermisivaSecurityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PermisivaSecurityAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain cadenaPermisiva(HttpSecurity http) throws Exception {
        log.warn("Seguridad PERMISIVA activa (C-01): todas las rutas son accesibles sin login hasta que se integre C-05.");
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // consola H2 en perfil dev
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(UsuarioActual.class)
    UsuarioActual usuarioActualPermisivo() {
        return () -> Solicitante.SISTEMA;
    }

    /** Evita que Spring genere un usuario en memoria con contraseña impresa en el log. C-05 lo reemplaza. */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    UserDetailsService sinUsuariosHastaC05() {
        return username -> {
            throw new UsernameNotFoundException("Autenticación no disponible hasta C-05");
        };
    }
}
